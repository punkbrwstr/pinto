package tech.pinto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Indexer implements Consumer<Table>, Cloneable {

	private final List<Index> indexes = new ArrayList<>();
	private final Pinto pinto;
	private final String indexString;
	private final boolean functionIndexer;

	public Indexer(Pinto pinto, String indexString) {
		this(pinto, indexString, false);
	}

	public Indexer(Pinto pinto, String indexString, boolean functionIndexer) {
		this.pinto = pinto;
		this.indexString = indexString;
		this.functionIndexer = functionIndexer;
		
		StringBuilder sb = new StringBuilder();
		final int[] open = new int[4]; // ", $, {, [
		for(int i = 0; i < indexString.length(); i++) {
			// first check what's open
			switch(indexString.charAt(i)) {
				case '"':	open[0] = open[0] == 0 ? 1 : 0;		break;
				case '$':	open[1] = open[1] == 0 ? 1 : 0;		break;
				case '{':	open[2]++;							break;
				case '}':	open[2]--;							break;
				case '[':	open[3]++;							break;
				case ']':	open[3]--;							break;
			}

			// don't count commas if anything's open 
			if(Arrays.stream(open).sum() > 0) { 
				sb.append(indexString.charAt(i));
			} else {
				if(indexString.charAt(i) == ',') {
					indexes.add(new Index(sb.toString().trim()));
					sb = new StringBuilder();
					Arrays.setAll(open, x -> 0);
				} else {
					 sb.append(indexString.charAt(i));
				}
			}
		}
		if(Arrays.stream(open).sum() == 0) { 
			indexes.add(new Index(sb.toString().trim()));
		} else {
			String unmatched = IntStream.range(0, 4).mapToObj(i -> open[i] == 0 ? "" : new String[]{"\"","$","{","["}[i])
					.filter(s -> !s.equals("")).collect(Collectors.joining(","));
			throw new IllegalArgumentException("Unmatched \"" + unmatched + "\" in Index: \"[" + indexString + "]\"");
		}
	}

	@Override
	public void accept(Table t) {
		LinkedList<LinkedList<Column<?,?>>> unused = new LinkedList<>();
		LinkedList<LinkedList<Column<?,?>>> indexed = new LinkedList<>();
		for (LinkedList<Column<?,?>> stack : t.takeTop()) {
			List<StackOperation> ops = new ArrayList<>();
				indexed.addLast(operate(stack, ops, pinto));
				Index last = indexes.get(indexes.size() - 1);
				while (last.isRepeat() && stack.size() > 0) {
					try {
						indexed.addLast(operate(stack, last.index(stack), pinto));
					} catch (IllegalArgumentException pse) {
						break;
					}
				}
			unused.add(stack);
		}
		t.insertAtTop(unused);
		t.push(functionIndexer, indexed);

	}

	@SuppressWarnings("unchecked")
	private LinkedList<Column<?,?>> operate(LinkedList<Column<?,?>> stack, List<StackOperation> ops, Pinto pinto) {
		indexes.stream().map(i -> i.index(stack)).forEach(ops::addAll);
		List<StackOperation> indexStringOps = ops.stream().filter(StackOperation::isHeader).collect(Collectors.toList());
		List<StackOperation> ordinalOps = ops.stream().filter(so -> ! so.isHeader()).collect(Collectors.toList());
		// determine which operations need to be copies
		LinkedList<StackOperation>[] opsByOrdinal = new LinkedList[stack.size()];
		for(List<StackOperation> l : Arrays.asList(indexStringOps, ordinalOps)) {
			for (StackOperation op : l) {
				if (!op.isAlternative()) {
					if (opsByOrdinal[op.getOrdinal()] != null) {
						if(opsByOrdinal[op.getOrdinal()].getLast().isHeader() &&
								!opsByOrdinal[op.getOrdinal()].getLast().isHeader()) {
							op.setSkip(true); // don't include cols index by indexString in subsequent ordinal indexes
						} else {
							opsByOrdinal[op.getOrdinal()].getLast().setNeedsCloning(true);
						}
					} else {
						opsByOrdinal[op.getOrdinal()] = new LinkedList<>();
					}
					if(!op.skip()) {
						opsByOrdinal[op.getOrdinal()].add(op);
					}
				}
			}
		}

		TreeSet<Integer> alreadyUsed = new TreeSet<>();
		LinkedList<Column<?,?>> indexed = new LinkedList<>();
		for (StackOperation o : ops) {
			if (o.isAlternative()) {
				indexed.addAll(pinto.parseSubExpression(o.getAlternativeString()));
				if(o.isCopy()) {
					stack.addAll(pinto.parseSubExpression(o.getAlternativeString()));
				}
			} else if ((!alreadyUsed.contains(o.getOrdinal())) && !o.skip()) {
				Column<?,?> c = stack.get(o.getOrdinal());
				indexed.add(o.checkType(o.needsCloning() || o.isCopy() ? c.clone() : c));
				if (!o.isCopy()) {
					alreadyUsed.add(o.getOrdinal());
				}
			}
		}
		for (int i = opsByOrdinal.length - 1; i >= 0; i--) {
			if (opsByOrdinal[i] != null) {
				opsByOrdinal[i].stream().filter(((Predicate<StackOperation>) StackOperation::isCopy).negate()).findAny()
						.ifPresent(op -> stack.remove(op.getOrdinal()));
			}
		}
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

	private class Index {

		private Optional<String> indexString = Optional.empty();
		private Optional<Integer> ordinal = Optional.empty();
		private Optional<Integer> sliceStart = Optional.empty();
		private Optional<Integer> sliceEnd = Optional.empty();
		private Optional<String> or = Optional.empty();
		private boolean copy = false;
		private boolean repeat = false;
		private boolean optional = false;
		private boolean everything = false;
		private boolean checkString = false;
		private boolean checkConstant = false;

		public Index(String s) {
			if (s.contains("&")) {
				if (repeat) {
					throw new PintoSyntaxException(
							"Cannot copy and repeat an index because it will create an infinite loop.");
				}
				copy = true;
				s = s.replace("&", "");
			}
			if (s.contains("=")) {
				String[] thisOrThat = s.split("=");
				if (thisOrThat.length < 2) {
					throw new IllegalArgumentException("\"=\" should be followed by alternative expression in index:" + s);
				}
				String alt = Arrays.stream(thisOrThat).skip(1).collect(Collectors.joining("="));
				or = Optional.of(" {" + thisOrThat[0] + ": " + alt + "}");
				s = thisOrThat[0];
			}
			if (s.contains("+")) {
				repeat = true;
				s = s.replace("+", "");
			}
			if (s.contains("?")) {
				optional = true;
				s = s.replace("?", "");
			}
			if (s.contains("@")) {
				checkString = true;
				s = s.replace("@", "");
			}
			if (s.contains("#")) {
				if (checkString) {
					throw new PintoSyntaxException("Cannot enforce both # and @ types.");
				}
				checkConstant = true;
				s = s.replace("#", "");
			}
			if (s.equals("")) {
				// none index
			} else if (s.equals(":")) {
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
					indexString = Optional.of(s);
				}
			}

		}

		public List<StackOperation> index(LinkedList<Column<?,?>> stack) {
			List<StackOperation> ops = new ArrayList<>();
//			if (stack.size() == 0 && ! or.isPresent()) {
//				return ops;
//			}
			if (everything || sliceStart.isPresent() || ordinal.isPresent()) {
				int start = 0, end = 0;
				if (everything) {
					if(stack.isEmpty()) {
						return ops;
					}
					start = 0;
					end = stack.size();
				} else if (sliceStart.isPresent()) {
					start = sliceStart.get();
					start = start < 0 ? start + stack.size() : start;
					end = sliceEnd.get();
					end = end < 0 ? end + stack.size() : end;
				} else if (ordinal.isPresent()) {
					start = ordinal.get();
					start = start < 0 ? start + stack.size() : start;
					end = start + 1;
				}
				if (start >= end) {
					throw new IllegalArgumentException("Invalid index. Start is after end.");
				}
				if (start < 0 || start >= stack.size()) {
					if (!optional) {
						throw new IllegalArgumentException(
								"Index [" + start + ":" + end + "] is outside bounds of stack.");
					}
				} else {
					for (int n = start; n < end && n < stack.size(); n++) {
						ops.add(new StackOperation(n, isCopy(), false, checkString, checkConstant));
					}
				}
			} else if (indexString.isPresent()) {
				final String query = indexString.get();
				Predicate<String> test;
				if (query.startsWith("*")) {
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
				for (int n = 0; n < stack.size(); n++) {
					if (test.test(stack.get(n).getHeader())) {
						ops.add(new StackOperation(n, isCopy(), true, checkString, checkConstant));
						found = true;
					}
				}
				if (!found && !optional) {
					if (or.isPresent()) {
						ops.add(new StackOperation(or.get(), checkString, checkConstant));
					} else {
						throw new IllegalArgumentException("Missing required indexString \"" + query + "\"");
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
		private final boolean isHeader;
		private final boolean checkString;
		private final boolean checkConstant;
		private final boolean copy;
		private boolean skip = false;
		private boolean needsCloning = false;
		private Optional<String> alternative = Optional.empty();

		public StackOperation(int ordinal, boolean copy, boolean isHeader, boolean checkString, boolean checkConstant) {
			this.ordinal = ordinal;
			this.copy = copy;
			this.checkConstant = checkConstant;
			this.checkString = checkString;
			this.isHeader = isHeader;
		}

		public StackOperation(String alternative, boolean checkString, boolean checkConstant) {
			this.alternative = Optional.of(alternative);
			ordinal = -1;
			copy = false;
			this.checkConstant = checkConstant;
			this.checkString = checkString;
			this.isHeader = false;
		}

		public boolean isAlternative() {
			return alternative.isPresent();
		}

		public String getAlternativeString() {
			return alternative.get();
		}

		public int getOrdinal() {
			return ordinal;
		}

		public boolean isCopy() {
			return copy;
		}

		public void setNeedsCloning(boolean needsCloning) {
			this.needsCloning = needsCloning;
		}
		
		public boolean needsCloning() {
			return needsCloning;
		}

		public void setSkip(boolean skip) {
			this.skip = skip;
		}
		
		public boolean skip() {
			return skip;
		}
		
		public boolean isHeader() {
			return isHeader;
		}

		public Column<?,?> checkType(Column<?,?> c) throws PintoSyntaxException {
			if(checkString && ! (c instanceof Column.OfConstantStrings)) {
				throw new PintoSyntaxException("String column required.");
			} else if(checkConstant && ! (c instanceof Column.OfConstantDoubles)) {
				throw new PintoSyntaxException("Constant column required.");
			}
			return c;
		}

//		public LinkedList<Column> checkType(LinkedList<Column> l) throws Exception {
//			for (Column c : l) {
//				checkType(c);
//			}
//			return l;
//		}

		@Override
		public int compareTo(StackOperation o) {
			return Integer.valueOf(ordinal).compareTo(Integer.valueOf(o.getOrdinal()));
		}

	}

}
