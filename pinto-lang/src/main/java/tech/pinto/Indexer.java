package tech.pinto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;

public class Indexer implements Cloneable {
	public static Indexer ALL = new Indexer();

	private List<Index> indexes = new ArrayList<>();
	private String indexString;
	
	private Indexer()  {
		this.indexString = ":";
		try {
			indexes.add(new Index(":"));
		} catch (PintoSyntaxException e) {}
	}

	public Indexer(String indexString) throws PintoSyntaxException {
		this.indexString = indexString;
		String[] indexParts = indexString.split(",");
		for(int n = 0; n < indexParts.length; n++) {
			indexes.add(new Index(indexParts[n].trim()));
		}
	}

	public List<LinkedList<Column>> index(LinkedList<Column> stack) throws PintoSyntaxException {
		List<LinkedList<Column>> indexed = new ArrayList<>();
		List<StackOperation> ops = new ArrayList<>();
		for(Index i : indexes) {
			ops.addAll(i.index(stack));
		}
		indexed.add(operate(stack,ops));
		Index last = indexes.get(indexes.size()-1);
		while(last.isRepeat() && stack.size() > 0) {
			try {
				indexed.add(operate(stack,last.index(stack)));
			} catch(PintoSyntaxException pse) {
				break;
			}
		}
		return indexed;
	}
	
	private LinkedList<Column> operate(LinkedList<Column> stack, List<StackOperation> ops) {
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
		new TreeSet<>(ops).descendingIterator().forEachRemaining(o -> {
			if(!o.isCopy()) {
				stack.remove(o.getOrdinal());
			}
		});
		return indexed;
	}


	private static void checkIndex(int index, int stackSize, boolean exclusive) throws PintoSyntaxException {
		if (index < 0 || (exclusive && index > stackSize) || (!exclusive && index >= stackSize)) {
			throw new PintoSyntaxException( index + " is outside bounds of inputs.");
		}
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
		
		public Index(String s) throws PintoSyntaxException {
			if(s.contains("|")) {
				String[] thisOrThat = s.split("\\|");
				if(thisOrThat.length != 2) {
					throw new PintoSyntaxException("Index \"|\" should separate a pair of index expressions.");
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
					throw new PintoSyntaxException(
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
		
		public List<StackOperation> index(LinkedList<Column> stack) throws PintoSyntaxException {
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
					throw new PintoSyntaxException("Invalid index. Start is after end.");
				}
				checkIndex(start, stack.size(), false);
				checkIndex(end, stack.size(), true);
				for(int n = start; n < end; n++) {
					ops.add(new StackOperation(n, isCopy()));
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
						throw new PintoSyntaxException("Missing required index header \"" + query + "\"");
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
		private final boolean copy;

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

		@Override
		public int compareTo(StackOperation o) {
			return new Integer(ordinal).compareTo(new Integer(o.getOrdinal()));
		}
		
	}

}
