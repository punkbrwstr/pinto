package tech.pinto;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import tech.pinto.function.EvaluableFunction;

public class Indexer implements Cloneable {
	
	private boolean everything = true;
	private boolean none = false;
	private Integer start = null;
	private Integer end = null;
	private TreeMap<Integer,Integer> indicies = new TreeMap<>(Collections.reverseOrder());
	private List<String> labelIndicies = null;
	private String indexString;
	private Optional<Indexer> chainedIndexer = Optional.empty();
	
	public static Indexer ALL = new Indexer();
	
	private Indexer() {
		indexString = "ALL";
	}

	public Indexer(String indexString) throws PintoSyntaxException {
		this(indexString, Optional.empty());
	}

	public Indexer(String indexString, Indexer toChain) throws PintoSyntaxException {
		this(indexString, Optional.of(toChain));
	}

	public Indexer(String indexString, Optional<Indexer> toChain) throws PintoSyntaxException {
		this.indexString = indexString;
		this.chainedIndexer = toChain;
		everything = false;
		if(indexString.contains(":") && indexString.contains(",")) {
			throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Cannot combine range indexing with multiple indexing.");
		} else if (indexString.equals("x")) { // none index
			none = true;
		} else if (!indexString.contains(":")) { // it's a list of indicies
			String[] s = indexString.split(",");
			if(s.length == 0) {
				return;
			}
			if(isNumeric(s[0])) {
				int[] ia = Stream.of(s).mapToInt(Integer::parseInt).toArray();
				for(int i = 0; i < ia.length; i++) {
					indicies.put(ia[i], i);
				}
			} else {
				labelIndicies = Arrays.asList(s);
			}
		} else if(indexString.equals(":")) {
			start = 0;
			end = -1;
		} else if(indexString.indexOf(":") == 0) {
			start = 0;
			end = Integer.parseInt(indexString.substring(1));
		} else if(indexString.indexOf(":") == indexString.length() - 1) {
			end = -1;
			start = Integer.parseInt(indexString.substring(0, indexString.length() - 1));
		} else {
			String[] parts = indexString.split(":");
			start = Integer.parseInt(parts[0]);
			end = Integer.parseInt(parts[1]);
		} 
	}
	
	public void addIndexerToChain(Indexer indexer) {
		chainedIndexer = Optional.of(indexer);
	}
	
	public LinkedList<EvaluableFunction> index(LinkedList<EvaluableFunction> stack) throws PintoSyntaxException {
		LinkedList<EvaluableFunction> indexed = new LinkedList<>();
		if(everything) {
			indexed.addAll(stack);
			stack.clear();
		} else if(none) {
			indexed = new LinkedList<>();
		} else {
			if(start != null) {
				start = start < 0 ? start + stack.size() : start;
				end = end < 0 ? end + stack.size() : end;
				if (start > end) {
					throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Start is after end.");
				} 
				checkIndex(start, stack.size());
				checkIndex(end, stack.size());
				IntStream.range(start,end + 1).forEach(i -> indicies.put(i,i));
			} else if(labelIndicies != null) {
				AtomicInteger i = new AtomicInteger();
				Map<String,Integer> labels = stack.stream()
						.collect(Collectors.toMap(EvaluableFunction::toString, f -> i.getAndIncrement()));
				for(int j = 0; j < labelIndicies.size(); j++) {
					if(!labels.containsKey(labelIndicies.get(j))) {
						throw new PintoSyntaxException("Unable to index by label \"" + labelIndicies.get(j) + "\"");
					}
					indicies.put(labels.get(labelIndicies.get(j)), j);
				}
			}
			TreeMap<Integer,EvaluableFunction> functions = new TreeMap<>();
			for(Map.Entry<Integer, Integer> i : indicies.entrySet()) {
				if(stack.size() == 0) {
					throw new PintoSyntaxException();
				}
				int index = i.getKey().intValue() < 0 ? i.getKey().intValue() + stack.size() : i.getKey().intValue();
				checkIndex(index, stack.size());
				functions.put(i.getValue(), stack.remove(index));
			}
			functions.values().stream().forEach(indexed::addLast);
		}
		LinkedList<EvaluableFunction> chained = indexed;
		if(chainedIndexer.isPresent()) {
			chained = chainedIndexer.get().index(indexed); 
			indexed.descendingIterator().forEachRemaining(stack::addFirst);
		}
		return chained;
	}

	private static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  
	
	private void checkIndex(int index, int stackSize) throws PintoSyntaxException {
		if (index < 0 || index >= stackSize) {
			throw new PintoSyntaxException("Invalid index \"" + indexString + "\": "  + index + " is outside bounds of inputs.");
		} 
	}
	
	public String toString() {
		return "[" + indexString + "]";
	}
	
	public Indexer clone() {
		try {
			Indexer clone = (Indexer) super.clone();
			clone.chainedIndexer = chainedIndexer.isPresent() ? Optional.of(chainedIndexer.get().clone()) : Optional.empty();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
