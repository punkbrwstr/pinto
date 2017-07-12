package tech.pinto;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.TreeMultimap;

import tech.pinto.tools.SearchableMultiMap;

public class Indexer implements Cloneable {
	
	private boolean everything = true;
	private boolean none = false;
	private boolean repeat = false;
	private boolean copy = false;
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
		if(indexString.contains("+")) {
			repeat = true;
			indexString = indexString.replace("+", "");
		}
		if(indexString.contains("&")) {
			if(repeat) {
				throw new PintoSyntaxException("Cannot copy and repeat an index because it will create an infinite loop.");
			}
			copy = true;
			indexString = indexString.replace("&", "");
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
			end = Integer.MAX_VALUE;
			start = Integer.parseInt(indexString.substring(0, indexString.length() - 1));
		} else {
			String[] parts = indexString.split(":");
			start = Integer.parseInt(parts[0]);
			end = Integer.parseInt(parts[1]);
		} 
	}
	
	public LinkedList<Column> index(LinkedList<Column> stack) throws PintoSyntaxException {
		LinkedList<Column> indexed = new LinkedList<>();
		if(everything) {
			if(!copy) {
				indexed.addAll(stack);
				stack.clear();
			} else {
				stack.stream().map(Column::clone).forEach(indexed::addLast);
			}
		} else if(none) {
			indexed = new LinkedList<>();
		} else {
			if(start != null) {
				start = start < 0 ? start + stack.size() : start;
				end = end < 0 ? end + stack.size() :
					end == Integer.MAX_VALUE ? stack.size() : end;
				if (start > end) {
					throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Start is after end.");
				} 
				checkIndex(start, stack.size(),false);
				checkIndex(end, stack.size(),true);
				//IntStream.range(start,end + 1).forEach(i -> indicies.put(i,i));
				IntStream.range(start,end).forEach(i -> indicies.put(i,i));
			} else if(labelIndicies != null) {
				SearchableMultiMap<Integer> labels =  new SearchableMultiMap<>();
				for(int i = 0; i < stack.size(); i++) {
					labels.add(stack.get(i).toString(), i);
				}
				int j = 0;
				for(String labelToGet : labelIndicies) {
					Optional<List<Integer>> results = labels.search(labelToGet);
					if(!results.isPresent()) {
						throw new PintoSyntaxException("Unable to index by label \"" + labelToGet + "\"");
					}
					for(int k : results.get()) {
						indicies.put(k, j++);
					}
				}
			}
			TreeMap<Integer,Column> functions = new TreeMap<>();
			for(Map.Entry<Integer, Collection<Integer>> e : indicies.asMap().entrySet()) {
				if(stack.size() == 0) {
					throw new PintoSyntaxException();
				}
				int index = e.getKey().intValue() < 0 ? e.getKey().intValue() + stack.size() : e.getKey().intValue();
				checkIndex(index, stack.size(),false);
				Column f = copy ? stack.get(index) : stack.remove(index);
				boolean needsCloning = copy;
				for(int i : e.getValue()) {
					functions.put(i, needsCloning ? f.clone() : f);
					needsCloning = true;
				}
			}
			functions.values().stream().forEach(indexed::addLast);
		}
		return indexed;
	}
	
	public boolean isRepeated() {
		return repeat;
	}

	public boolean isEverything() {
		return  everything;
	}

	public boolean isCopy() {
		return copy;
	}

	private static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  
	
	private void checkIndex(int index, int stackSize, boolean exclusive) throws PintoSyntaxException {
		if (index < 0 || (exclusive && index > stackSize) || (!exclusive && index >= stackSize)) {
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
