package tech.pinto;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.TreeMultimap;

import tech.pinto.function.EvaluableFunction;

public class Indexer implements Cloneable {
	
	private boolean everything = true;
	private boolean none = false;
	private boolean reverse = false;
	private Integer start = null;
	private Integer end = null;
	private TreeMultimap<Integer,Integer> indicies = TreeMultimap.create(Collections.reverseOrder(), Collections.reverseOrder());
	private List<String> labelIndicies = null;
	private String indexString;
	
	public static final Indexer ALL = new Indexer(true,false,"ALL");
	public static final Indexer NONE = new Indexer(false,true,"NONE");
	
	private Indexer(boolean everything, boolean none, String indexString) {
		this.everything = everything;
		this.none = none;
		this.indexString = indexString;
	}

	public Indexer(String indexString) throws PintoSyntaxException {
		this.indexString = indexString;
		everything = false;
		if(indexString.contains("~")) {
			reverse = true;
			indexString = indexString.replace("~", "");
		}
		if(indexString.contains(":") && indexString.contains(",")) {
			throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Cannot combine range indexing with multiple indexing.");
		} else if (indexString.equals("x")) { // none index
			none = true;
		} else if(indexString.equals(":") || indexString.equals("")) {
			everything = true;
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
				LinkedListMultimap<String,Integer> labels =  LinkedListMultimap.create();
				for(int i = 0; i < stack.size(); i++) {
					labels.put(stack.get(i).toString(), i);
				}
				int j = 0;
				for(String labelToGet : labelIndicies) {
					if(!labels.containsKey(labelToGet)) {
						throw new PintoSyntaxException("Unable to index by label \"" + labelToGet + "\"");
					}
					for(int k : labels.get(labelToGet)) {
						indicies.put(k, j++);
					}
				}
			}
			TreeMap<Integer,EvaluableFunction> functions = new TreeMap<>();
			for(Map.Entry<Integer, Collection<Integer>> e : indicies.asMap().entrySet()) {
				if(stack.size() == 0) {
					throw new PintoSyntaxException();
				}
				int index = e.getKey().intValue() < 0 ? e.getKey().intValue() + stack.size() : e.getKey().intValue();
				checkIndex(index, stack.size());
				EvaluableFunction f = stack.remove(index);
				for(int i : e.getValue()) {
					functions.put(i, f);
				}
			}
			functions.values().stream().forEach(indexed::addLast);
		}
		return indexed;
	}
	
	public boolean isReverse() {
		return reverse;
	}

	public boolean isEverything() {
		return  everything;
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
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
