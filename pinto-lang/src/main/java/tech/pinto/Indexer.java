package tech.pinto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import tech.pinto.Pinto.Expression;
import tech.pinto.Pinto.TableFunction;
import tech.pinto.Pinto.Stack;

public class Indexer implements Cloneable, TableFunction {

	private final List<Index> indexes = new ArrayList<>();
	private final String indexString;
	private boolean indexForExpression = false;

	public Indexer(boolean indexForExpression) {
		indexString = "";
		indexes.add(new Index(null,null,""));
		this.indexForExpression = indexForExpression;
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

	public void accept(Pinto pinto, Table t) {
		LinkedList<Stack> unused = new LinkedList<>();
		LinkedList<Stack> indexedStacks = new LinkedList<>();

		for (Stack stack : t.takeTop()) {
			boolean[] keepACopy = new boolean[stack.size()];
			boolean[] indexed = new boolean[stack.size()];
			List<int[]> unrepeatedOrdinals = new ArrayList<>();
			//int repeats = 1;
			for(int i = 0; i < indexes.size(); i++) {
				Index index = indexes.get(i);
				int[] ordinals = index.getOrdinals(stack, indexed);
				unrepeatedOrdinals.add(ordinals);
				if(index.isCopy()) {
					for(int j = 0; j < ordinals.length; j++) {
						keepACopy[ordinals[j]] = true;
					}
				}
			}
			List<int[][]> stackOrdinals = new ArrayList<>();
			stackOrdinals.add(new int[indexes.size()][]);
			for(int i = 0; i < indexes.size(); i++) {
				int[] ordinals= unrepeatedOrdinals.get(i);
				int repeat = indexes.get(i).getRepeat();
				List<int[][]> output = new ArrayList<>();
				for(int j = 0; j < stackOrdinals.size(); j++) {
					int[][] permutation = stackOrdinals.get(j);
					if(repeat == 0) {
						permutation[i] = ordinals;
						output.add(permutation);
					} else {
						for(int k = 0; k < ordinals.length - repeat + 1; k += repeat) {
							int[][] newPermutation = permutation.clone();
							newPermutation[i] = Arrays.copyOfRange(ordinals,k,k + repeat);
							output.add(newPermutation);
						}
					}
				}
				stackOrdinals = output;
			}
			boolean[] used = new boolean[stack.size()];
			for(int s = 0; s < stackOrdinals.size(); s++) {
				Stack thisStack = new Stack();
				int[][] ordinals = stackOrdinals.get(s);
				for(int i = 0; i < ordinals.length; i++) {
					for(int j = 0; j < ordinals[i].length; j++) {
						int ordinal = ordinals[i][j];
						if(ordinal == -1) {
							Table table = new Table();
							indexes.get(i).orFunction.get().accept(pinto, table);
							thisStack.addAll(table.flatten());
						} else {
							thisStack.addLast(used[ordinal] || keepACopy[ordinal] ? stack.get(ordinal).clone() : stack.get(ordinal));
							used[ordinal] = true;
						}
					}
				}
				indexedStacks.addLast(thisStack);
			}
			Stack thisUnused = new Stack();
			for(int i = 0; i < used.length; i++) {
				if(keepACopy[i] || !used[i]) {
					thisUnused.addLast(stack.get(i));
				}
			}
			unused.addLast(thisUnused);
		}
		t.insertAtTop(unused);
		t.push(indexForExpression, indexedStacks);
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
		private boolean copy = false;
		private boolean not = false;
		private int repeat = 0;
		private Optional<String> header = Optional.empty();
		private Optional<Integer> sliceStart = Optional.empty();
		private Optional<Integer> sliceEnd = Optional.empty();
		private Optional<Expression> orFunction = Optional.empty();

		public Index(Pinto pinto, Set<String> dependencies, String s) {
			this.string = s;
			if(!s.isEmpty()) {
				int equalsPosition = s.indexOf("=");
				if (equalsPosition != -1) {
					if(equalsPosition == s.length()-1) {
						throw new IllegalArgumentException("\"=\" should be followed by alternative expression in index:" + s);
					}
					String header = s.substring(0, equalsPosition);
					String altExpression = s.substring(equalsPosition+1);
					Pinto.Expression e = pinto.parseSubExpression(" {" + header + ": " + altExpression + "}");
					dependencies.addAll(e.getDependencies());
					orFunction = Optional.of(e);
					s = header;
				} 
				if (s.contains("&")) {
					copy = true;
					s = s.replace("&", "");
				}
				if (s.contains("!")) {
					not = true;
					s = s.replace("!", "");
				}
				int plusPosition = s.indexOf("+");
				if (plusPosition != -1) {
					repeat = plusPosition == s.length()-1 ? 1 : Integer.parseInt(s.substring(plusPosition+1));
					s = s.substring(0,plusPosition);
				}
				int colonPosition = s.indexOf(":");
				if(colonPosition != -1) {
					sliceStart = Optional.of(colonPosition == 0 ? 0 : Integer.valueOf(s.substring(0,colonPosition)));
					sliceEnd = Optional.of(colonPosition == s.length()-1 ? Integer.MAX_VALUE : Integer.valueOf(s.substring(colonPosition+1)));
				} else {
					if (s.matches("[-+]?\\d*\\.?\\d+")) {
						sliceStart = Optional.of(Integer.parseInt(s));
					} else {
						header = Optional.of(s);
					}
				}
			}

		}

		public int[] getOrdinals(Stack stack, boolean[] used) {
			List<Integer> ordinals = new ArrayList<>();
			// slice based index
			if(sliceStart.isPresent()) {
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
				if (start < 0 || end > stack.size()) {
					throw new IllegalArgumentException("Index [" + start + ":" + end + "] is outside bounds of stack.");
				} 
				for (int n = start; n < end; n++) {
					if(!used[n]) {
						ordinals.add(n);
						//used[n] = true;
					}
				}
			} else if(header.isPresent()){
				final String query = header.get();
				Predicate<String> test;
				int starPosition = query.indexOf("*");
				if(starPosition != -1) {
					Predicate<String> starts = s -> starPosition == 0 ? true : s.startsWith(query.substring(0,starPosition));
					Predicate<String> ends = s -> starPosition == query.length()-1 ? true : s.endsWith(query.substring(starPosition+1));
					test = starts.and(ends); 
				} else {
					test = s -> s.equals(query);
				}
				for (int n = 0; n < stack.size(); n++) {
					if (test.test(stack.get(n).getHeader())) {
						ordinals.add(n);
						used[n] = true;
					}
				}
				if(ordinals.isEmpty()) {
					if(orFunction.isPresent()) {
						ordinals.add(-1);
					} else {
						throw new IllegalArgumentException("Missing header \"" + query + "\".");
					}
				}
			}
			if(not) {
				List<Integer> temp = new ArrayList<>(ordinals);
				ordinals.clear();
				for (int n = 0; n < stack.size(); n++) {
					if(!temp.contains(n)) {
						ordinals.add(n);
					}
				}
			}
			return ordinals.stream().mapToInt(Integer::intValue).toArray();
		}

		public boolean isNone() {
			return !(sliceStart.isPresent() || header.isPresent());
		}

		public boolean isCopy() {
			return copy;
		}

		public int getRepeat() {
			return repeat;
		}

		public String toString() {
			return string;
		}

	}

}
