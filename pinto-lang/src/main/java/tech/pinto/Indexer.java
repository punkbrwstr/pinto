package tech.pinto;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import tech.pinto.function.Function;
import tech.pinto.function.PlaceholderFunction;

public class Indexer {
	
	private boolean everything = true;
	private Integer start = null;
	private Integer end = null;
	private TreeMap<Integer,Integer> indicies = new TreeMap<>(Collections.reverseOrder());
	private List<String> labelIndicies = null;
	
	public static Indexer ALL = new Indexer();
	
	private Indexer() {}

	public Indexer(String indexString, LinkedList<Function> stack) throws PintoSyntaxException {
		everything = false;
		if(indexString.contains(":") && indexString.contains(",")) {
			throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Cannot combine range indexing with multiple indexing.");
		} else if (!indexString.contains(":")) { // it's a list of indicies
			String[] s = indexString.split(",");
			if(s.length == 0) {
				return;
			}
			if(isNumeric(s[0])) {
				int[] ia = Stream.of(s).mapToInt(Integer::parseInt).toArray();
				for(int i = 0; i < ia.length; i++) {
					indicies.put(ia[i] < 0 ? ia[i] + stack.size() : ia[i], i);
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
		
		int placeholdersNeeded = 0;
		if(start != null) {
			start = start < 0 ? start + stack.size() : start;
			end = end < 0 ? end + stack.size() : end;
			if (start > end) {
				throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Start is after end.");
			} else if (start < 0) {
				//throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Start is too low.");
				placeholdersNeeded = -1 * start;
			} else if (end >= stack.size()) {
				//throw new PintoSyntaxException("Invalid index \"" + indexString + "\". End too high for stack size.");
				placeholdersNeeded = end - stack.size() + 1;
			}
		} else {
			for(int i : indicies.keySet()) {
				if (i < 0) {
					//throw new PintoSyntaxException("Invalid index \"" + i + "\". Start is too low.");
					placeholdersNeeded = Math.max(placeholdersNeeded, -1 * i);
				} else if (i >= stack.size()) {
					//throw new PintoSyntaxException("Invalid index \"" + i + "\". End too high for stack size.");
					placeholdersNeeded = Math.max(placeholdersNeeded, i - stack.size() + 1);
				}
				
			}
		}
		for(int i = 0; i < placeholdersNeeded; i++) {
			stack.addLast(new PlaceholderFunction("Index(" + indexString + ")"));
		}
	}
	
	public LinkedList<Function> index(LinkedList<Function> stack) throws PintoSyntaxException {
		LinkedList<Function> indexed = new LinkedList<>();
		if(everything) {
			indexed.addAll(stack);
			stack.clear();
		} else {
			if(start != null) {
				IntStream.range(start,end + 1).forEach(i -> indicies.put(i,i));
			} else if(labelIndicies != null) {
				AtomicInteger i = new AtomicInteger();
				Map<String,Integer> labels = stack.stream()
						.collect(Collectors.toMap(Function::toString, f -> i.getAndIncrement()));
				for(int j = 0; j < labelIndicies.size(); j++) {
					if(!labels.containsKey(labelIndicies.get(j))) {
						throw new PintoSyntaxException("Unable to index by label \"" + labelIndicies.get(j) + "\"");
					}
					indicies.put(labels.get(labelIndicies.get(j)), j);
				}
			}
			TreeMap<Integer,Function> functions = new TreeMap<>();
			for(Map.Entry<Integer, Integer> i : indicies.entrySet()) {
				if(stack.size() == 0) {
					throw new PintoSyntaxException();
				}
				functions.put(i.getValue(), stack.remove(i.getKey().intValue()));
			}
			functions.values().stream().forEach(indexed::addLast);
		}
		return indexed;
	}

	private static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  

}
