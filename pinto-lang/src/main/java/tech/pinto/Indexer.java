package tech.pinto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;

public class Indexer implements Cloneable {
	public static Indexer ALL = new Indexer(":");

	private List<Index> indexes = new ArrayList<>();
	private String indexString;
	
	public Indexer(String indexString) {
		indexString = indexString.replaceAll("\\[|\\]", "");
		this.indexString = indexString;
		String[] indexParts = indexString.split(",");
		for(int n = 0; n < indexParts.length; n++) {
			indexes.add(new Index(indexParts[n].trim()));
		}
	}

	public List<LinkedList<Column>> index(LinkedList<Column> stack) {
		List<LinkedList<Column>> indexed = new ArrayList<>();
		List<StackOperation> ops = new ArrayList<>();

		indexed.add(operate(stack,ops));
		Index last = indexes.get(indexes.size()-1);
		while(last.isRepeat() && stack.size() > 0) {
			try {
				indexed.add(operate(stack,last.index(stack)));
			} catch(IllegalArgumentException pse) {
				break;
			}
		}
		return indexed;
	}
	
	private LinkedList<Column> operate(LinkedList<Column> stack, List<StackOperation> ops) {
		@SuppressWarnings("unchecked")
		LinkedList<StackOperation>[] opsByOrdinal = new LinkedList[stack.size()];
		for(Index i : indexes) {
			List<StackOperation> opsForIndex = i.index(stack);
			for(StackOperation op: opsForIndex) {
				if(opsByOrdinal[op.getOrdinal()] != null) {
					opsByOrdinal[op.getOrdinal()].getLast().setCopy();
				} else {
					opsByOrdinal[op.getOrdinal()] = new LinkedList<>();
				}
				opsByOrdinal[op.getOrdinal()].add(op);
			}
			ops.addAll(opsForIndex);
		}
		TreeSet<Integer> alreadyUsed = new TreeSet<>();
		LinkedList<Column> indexed = new LinkedList<>();
		for(StackOperation o : ops) {
			if(!alreadyUsed.contains(o.getOrdinal())) {
				Column c = stack.get(o.getOrdinal());
				indexed.add(o.isCopy() ? c.clone() : c);
				if(!o.isCopy()) {
					alreadyUsed.add(o.getOrdinal());
				}
			}
		}
		for(int i = opsByOrdinal.length - 1; i >= 0; i--) {
			if(opsByOrdinal[i] != null) {
				opsByOrdinal[i].stream().filter(((Predicate<StackOperation>) StackOperation::isCopy).negate())
					.findAny().ifPresent(op -> stack.remove(op.getOrdinal()));
			}
		}
//		new TreeSet<>(ops).descendingIterator().forEachRemaining(o -> {
//			if(!o.isCopy()) {
//				stack.remove(o.getOrdinal());
//			}
//		});
		return indexed;
	}

	private static boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
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
	

	private static class Index {

		private Optional<String> header = Optional.empty();
		private Optional<Integer> ordinal = Optional.empty();
		private Optional<Integer> sliceStart = Optional.empty();
		private Optional<Integer> sliceEnd = Optional.empty();
		private Optional<Index> or = Optional.empty();
		private boolean copy = false;
		private boolean repeat = false;
		private boolean optional = false;
		private boolean everything = false;
		
		public Index(String s) {
			if(s.contains("|")) {
				String[] thisOrThat = s.split("\\|");
				if(thisOrThat.length != 2) {
					throw new IllegalArgumentException("Index \"|\" should separate a pair of index expressions.");
				}
				or = Optional.of(new Index(thisOrThat[1]));
				s = thisOrThat[0];
			}
			if(s.contains("+")) {
				repeat = true;
				s = s.replace("+", "");
			}
			if(s.contains("&")) {
				if (repeat) {
					throw new IllegalArgumentException(
							"Cannot copy and repeat an index because it will create an infinite loop.");
				}
				copy = true;
				s = s.replace("&", "");
			}
			if(s.contains("?")) {
				optional = true;
				s = s.replace("?", "");
			}
			if(s.equals("x")) {
				// none index
			} else if(s.equals(":") || s.equals("")) {
				everything = true;
			} else if (s.contains(":")) {
				if (s.indexOf(":") == 0) {
					sliceStart = Optional.of(0);
					sliceEnd = Optional.of(Integer.parseInt(s.substring(1)));
				} else if (s.indexOf(":") == s.length() - 1) {
					sliceEnd = Optional.of(Integer.MAX_VALUE);
					sliceStart = Optional.of(Integer.parseInt(s.substring(0, s.length() - 1)));
				} else {
					String[] parts = s.split(":");
					sliceStart = Optional.of(Integer.parseInt(parts[0]));
					sliceEnd = Optional.of(Integer.parseInt(parts[1]));
				}

			} else {
				if (isNumeric(s)) {
					ordinal = Optional.of(Integer.parseInt(s));
				} else {
					header = Optional.of(s);
				}
			}
			
		}
		
		public List<StackOperation> index(LinkedList<Column> stack) {
			List<StackOperation> ops = new ArrayList<>();
			if (stack.size() == 0) {
				return ops;
			}
			if (everything || sliceStart.isPresent() || ordinal.isPresent()) {
				int start = 0, end = 0;
				if(everything) {
					start = 0;
					end = stack.size();
				} else if(sliceStart.isPresent()) {
					start = sliceStart.get();
					start = start < 0 ? start + stack.size() : start;
					end = sliceEnd.get();
					end = end < 0 ? end + stack.size() : end == Integer.MAX_VALUE ? stack.size() : end;
				} else if(ordinal.isPresent()) {
					start = ordinal.get();
					start = start < 0 ? start + stack.size() : start;
					end = start + 1;
				}
				if (start >= end) {
					throw new IllegalArgumentException("Invalid index. Start is after end.");
				}
				if (start < 0 || start >= stack.size() || end > stack.size()) {
					if(!optional) {
						throw new IllegalArgumentException( "Index [" + start + ":" + end + "] is outside bounds of inputs.");
					} 
				} else {
					for(int n = start; n < end; n++) {
						ops.add(new StackOperation(n, isCopy()));
					}
				}
			} else if(header.isPresent()) {
				final String query = header.get();
				Predicate<String> test;
				if(query.startsWith("*")) {
					final String toFind = query.substring(1);
					test = s -> s.endsWith(toFind);
				} else if (query.endsWith("*")) {
					final String toFind = query.substring(0, query.length() - 1);
					test = s -> s.startsWith(toFind);
				} else if (query.contains("*")) {
					final String[] toFind = query.split("\\*"); 
					test = s -> s.startsWith(toFind[0]) && s.endsWith(toFind[1]);
				} else {
					test = s -> s.equals(query);
				}
				boolean found = false;
				for(int n = 0; n < stack.size(); n++) {
					if(test.test(stack.get(n).getHeader())) {
						ops.add(new StackOperation(n, isCopy()));
						found = true;
					}
				}
				if(!found && !optional) {
					if(or.isPresent()) {
						ops.addAll(or.get().index(stack));
					} else {
						throw new IllegalArgumentException("Missing required index header \"" + query + "\"");
					}
				}
			}
			return ops;
			
		}

		public boolean isCopy() {
			return copy;
		}

		public boolean isRepeat() {
			return repeat;
		}

	}

	private static class StackOperation implements Comparable<StackOperation> {
		private final int ordinal;
		private boolean copy;

		public StackOperation(int ordinal, boolean copy) {
			this.ordinal = ordinal;
			this.copy = copy;
		}
		
		public int getOrdinal() {
			return ordinal;
		}

		public boolean isCopy() {
			return copy;
		}
		
		public void setCopy() {
			copy = true;
		}

		@Override
		public int compareTo(StackOperation o) {
			return new Integer(ordinal).compareTo(new Integer(o.getOrdinal()));
		}
		
	}

}
