package tech.pinto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import tech.pinto.Pinto.Expression;

public class Indexer implements Cloneable, Consumer<Table> {

	private final List<Index> indexes = new ArrayList<>();
	private final String indexString;
	private boolean indexForExpression = false;

	public Indexer() {
		indexString = "";
		indexes.add(new Index(null,null,""));
	}

	public Indexer(Pinto pinto, Set<String> dependencies, String indexString)  {
		this.indexString = indexString.trim().replaceAll("^\\[|\\]$", "");

		StringBuilder sb = new StringBuilder();
		final int[] open = new int[4]; // ", $, {, [
		for (int i = 0; i < this.indexString.length(); i++) {
			// first check what's open
			switch (this.indexString.charAt(i)) {
			case '"': open[0] = open[0] == 0 ? 1 : 0; break;
			case '$': open[1] = open[1] == 0 ? 1 : 0; break;
			case '{': open[2]++; break;
			case '}': open[2]--; break;
			case '[': open[3]++; break;
			case ']': open[3]--; break;
			}

			// don't count commas if anything's open
			if (Arrays.stream(open).sum() > 0) {
				sb.append(this.indexString.charAt(i));
			} else {
				if (this.indexString.charAt(i) == ',') {
					indexes.add(new Index(pinto, dependencies, sb.toString().trim()));
					sb = new StringBuilder();
					Arrays.setAll(open, x -> 0);
				} else {
					sb.append(this.indexString.charAt(i));
				}
			}
		}
		if (Arrays.stream(open).sum() == 0) {
			indexes.add(new Index(pinto, dependencies, sb.toString().trim()));
		} else {
			String unmatched = IntStream.range(0, 4)
					.mapToObj(i -> open[i] == 0 ? "" : new String[] { "\"", "$", "{", "[" }[i])
					.filter(s -> !s.equals("")).collect(Collectors.joining(","));
			throw new IllegalArgumentException("Unmatched \"" + unmatched + "\" in Index: \"[" + indexString + "]\"");
		}
	}
	
	public boolean isNone() {
		return indexes.size() == 1 && indexes.get(0).isNone();
	}
	
	public void setIndexForExpression() {
		this.indexForExpression = true;
	}

	public void accept(Table t) {
		LinkedList<LinkedList<Column<?>>> unused = new LinkedList<>();
		LinkedList<LinkedList<Column<?>>> indexed = new LinkedList<>();

		for (LinkedList<Column<?>> stack : t.takeTop()) {
			List<StackOperation> ops = new ArrayList<>();
			indexed.addLast(operate(stack, ops));
			Index last = indexes.get(indexes.size() - 1);
			while (last.isRepeat() && stack.size() > 0) {
				try {
					indexed.addLast(operate(stack, last.index(stack)));
				} catch (IllegalArgumentException pse) {
					break;
				}
			}
			unused.add(stack);
		}
		t.insertAtTop(unused);
		t.push(indexForExpression, indexed);
	}

	@SuppressWarnings("unchecked")
	private LinkedList<Column<?>> operate(LinkedList<Column<?>> stack, List<StackOperation> ops) {
		for(int i = 0; i < indexes.size(); i++) {
			ops.addAll(indexes.get(i).index(stack));
		}
		List<StackOperation> indexStringOps = ops.stream().filter(StackOperation::isHeader)
				.collect(Collectors.toList());
		List<StackOperation> ordinalOps = ops.stream().filter(so -> !so.isHeader()).collect(Collectors.toList());
		// determine which operations need to be copies
		LinkedList<StackOperation>[] opsByOrdinal = new LinkedList[stack.size()];
		for (List<StackOperation> l : Arrays.asList(indexStringOps, ordinalOps)) {
			for (StackOperation op : l) {
				if (!op.isAlternative()) {
					if (opsByOrdinal[op.getOrdinal()] != null) {
						if (opsByOrdinal[op.getOrdinal()].getLast().isHeader() && !op.isHeader()) {
							op.setSkip(true); // don't include cols index by indexString in subsequent ordinal indexes
						} else {
							opsByOrdinal[op.getOrdinal()].getLast().setNeedsCloning(true);
						}
					} else {
						opsByOrdinal[op.getOrdinal()] = new LinkedList<>();
					}
					if (!op.skip()) {
						opsByOrdinal[op.getOrdinal()].add(op);
					}
				}
			}
		}

		TreeSet<Integer> alreadyUsed = new TreeSet<>();
		LinkedList<Column<?>> indexed = new LinkedList<>();
		for (StackOperation o : ops) {
			if (o.isAlternative()) {
				Table t = new Table();
				o.getAlternativeExpression().accept(t);
				indexed.addAll(t.flatten());
				if (o.isCopy()) {
					t = new Table();
					o.getAlternativeExpression().accept(t);
					stack.addAll(t.flatten());
				}
			} else if ((!alreadyUsed.contains(o.getOrdinal())) && !o.skip()) {
				Column<?> c = stack.get(o.getOrdinal());
				indexed.add(o.needsCloning() || o.isCopy() ? c.clone() : c);
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

		private final String string;
		private final boolean copy;
		private final boolean repeat;
		private final Optional<String> header;
		private final Optional<Integer> sliceStart;
		private final Optional<Integer> sliceEnd;
		private final Optional<Expression> orFunction;

		public Index(Pinto pinto, Set<String> dependencies, String s) {
			this.string = s;
			if (s.contains("&")) {
				copy = true;
				s = s.replace("&", "");
			} else {
				copy = false;
			}
			if (s.contains("=")) {
				String[] thisOrThat = s.split("=");
				if (thisOrThat.length < 2) {
					throw new IllegalArgumentException(
							"\"=\" should be followed by alternative expression in index:" + s);
				}
				String alt = Arrays.stream(thisOrThat).skip(1).collect(Collectors.joining("="));
				Pinto.Expression e = pinto.parseSubExpression(" {" + thisOrThat[0] + ": " + alt + "}");
				dependencies.addAll(e.getDependencies());
				orFunction = Optional.of(e);
				s = thisOrThat[0];
			} else {
				orFunction = Optional.empty();
			}
			if (s.contains("+")) {
				if (copy) {
					throw new PintoSyntaxException(
							"Cannot copy and repeat an index because it will create an infinite loop.");
				}
				repeat = true;
				s = s.replace("+", "");
			} else {
				repeat = false;
			}
			if (s.equals("")) {
				sliceStart = Optional.of(0);
				sliceEnd = Optional.of(0);
				header = Optional.empty();
			} else if (s.equals(":")) {
				sliceStart = Optional.of(0);
				sliceEnd = Optional.of(Integer.MAX_VALUE);
				header = Optional.empty();
			} else if (s.contains(":")) {
				if (s.indexOf(":") == 0) {
					sliceStart = Optional.of(0);
					sliceEnd = Optional.of(Integer.parseInt(s.substring(1)));
				} else if (s.indexOf(":") == s.length() - 1) {
					sliceStart = Optional.of(Integer.parseInt(s.substring(0, s.length() - 1)));
					sliceEnd = Optional.of(Integer.MAX_VALUE);
				} else {
					String[] parts = s.split(":");
					sliceStart = Optional.of(Integer.parseInt(parts[0]));
					sliceEnd = Optional.of(Integer.parseInt(parts[1]));
				}
				header = Optional.empty();
			} else {
				if (isNumeric(s)) {
					sliceStart = Optional.of(Integer.parseInt(s));
					sliceEnd = Optional.empty();
					header = Optional.empty();
				} else {
					header = Optional.of(s);
					sliceStart = Optional.empty();
					sliceEnd = Optional.empty();
				}
			}

		}

		public List<StackOperation> index(LinkedList<Column<?>> stack) {
			List<StackOperation> ops = new ArrayList<>();
			if (sliceStart.isPresent()) {
				if(stack.isEmpty()) {
					return ops;
				}
				int end = 0;
				int start = sliceStart.get();
				start = start < 0 ? start + stack.size() : start;
				if(sliceEnd.isPresent()) {
					end = sliceEnd.get();
					end = end < 0 ? end + stack.size() : 
							end == Integer.MAX_VALUE ? stack.size() : end;
				} else {
					end = start + 1;
				} 
				if (start > end) {
					throw new IllegalArgumentException("Invalid index. Start is after end.");
				}
				if (start < 0 || start >= stack.size()) {
					throw new IllegalArgumentException(
								"Index [" + start + ":" + end + "] is outside bounds of stack.");
				} else {
					for (int n = start; n < end && n < stack.size(); n++) {
						ops.add(new StackOperation(n, isCopy(), false));
					}
				}
			} else {
				final String query = header.get();
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
						ops.add(new StackOperation(n, isCopy(), true));
						found = true;
					}
				}
				if (!found) {
					ops.add(new StackOperation(orFunction.orElseThrow(() -> new IllegalArgumentException("Missing required header \"" + query + "\""))));
				}
			}
			return ops;

		}

		public boolean isNone() {
			return (sliceStart.orElse(1) == 0) &&
					(sliceEnd.orElse(1) == 0) &&
					!header.isPresent();
		}

		public boolean isCopy() {
			return copy;
		}

		public boolean isRepeat() {
			return repeat;
		}

		public String toString() {
			return string;
		}

	}

	private static class StackOperation implements Comparable<StackOperation> {
		private final int ordinal;
		private final boolean isHeader;
		private final boolean copy;
		private boolean skip = false;
		private boolean needsCloning = false;
		private Optional<Pinto.Expression> alternative = Optional.empty();

		public StackOperation(int ordinal, boolean copy, boolean isHeader) {
			this.ordinal = ordinal;
			this.copy = copy;
			this.isHeader = isHeader;
		}

		public StackOperation(Pinto.Expression alternative) {
			this.alternative = Optional.of(alternative);
			ordinal = -1;
			copy = false;
			this.isHeader = false;
		}

		public boolean isAlternative() {
			return alternative.isPresent();
		}

		public Pinto.Expression getAlternativeExpression() {
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

		@Override
		public int compareTo(StackOperation o) {
			return Integer.valueOf(ordinal).compareTo(Integer.valueOf(o.getOrdinal()));
		}

	}

}
