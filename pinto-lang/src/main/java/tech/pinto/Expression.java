package tech.pinto;

import java.util.ArrayDeque;




import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.supplier.Literal;

public class Expression extends IntermediateFunction {

	private final ArrayDeque<TerminalFunction> terminalCommands = new ArrayDeque<>();
	private final Set<String> dependencies = new HashSet<>();
	private final String text;

	public Expression(Cache cache, Vocabulary vocab, String text) throws PintoSyntaxException {
		this(cache, vocab, text, new LinkedList<>());
	}

	public Expression(Cache cache, Vocabulary vocab, String text, LinkedList<Function> existingStack)
			throws PintoSyntaxException {
		super("statement", new LinkedList<>(existingStack));
		this.text = text;
		existingStack.clear();
		Indexer indexer = Indexer.ALL;
		List<String> expressionToSave = new ArrayList<>();
		try (Scanner sc = new Scanner(text)) {
			while (sc.hasNext()) {
				if (sc.hasNext(Pattern.compile("\\[.*"))) { // index
					indexer = new Indexer(parseIndexString(sc), inputStack.size());
				} else {
					Function c = null;
					if (sc.hasNextDouble()) { // double literal
						c = new Literal(sc.nextDouble());
						expressionToSave.add(c.toString());
					} else {
						String s = sc.next();
						String commandName = s.contains("(") ? s.substring(0, s.indexOf("(")) : s;
						if (vocab.commandExists(commandName)) { // it's a known command
							try {
								c = vocab.getCommand(commandName, cache, 
										indexer.index(inputStack), new ArrayList<>(expressionToSave), parseCommandArguments(text, s, sc));
							} catch (IllegalArgumentException e) {
								throw new PintoSyntaxException("Wrong arguments for " + commandName + ": " + e.getMessage(), e);
							}
							expressionToSave.add(c.toString());
						} else { // it's the name of a saved statement (hopefully)
							if (!cache.isSavedStatement(s)) {
								throw new PintoSyntaxException("Command \"" + s + "\" not found.");
							}
							dependencies.add(s);
							c = new Expression(cache, vocab, cache.getSaved(s), indexer.index(inputStack));
							expressionToSave.add(s);
						}
						indexer = Indexer.ALL;
					}
					if (c instanceof TerminalFunction) {
						terminalCommands.addFirst((TerminalFunction) c);
						expressionToSave.clear();
					} else {
						for (int i = 0; i < c.getOutputCount(); i++) {
							inputStack.addFirst(c.getReference());
						}
					}
				}
			}
		}
		outputCount = inputStack.size();
	}

	@Override
	public Function getReference() {
		return inputStack.removeLast();
	}

	@Override
	public Set<String> getDependencies() {
		return dependencies;
	}

	public LinkedList<Function> getStack() {
		return inputStack;
	}

	public ArrayDeque<TerminalFunction> getTerminalCommands() {
		return terminalCommands;
	}

	@Override
	public String toString() {
		return text;
	}

	private String[] parseCommandArguments(String statement, String s, Scanner sc) {
		String[] args = null;
		if (s.contains("(") || sc.hasNext(Pattern.compile("\\(.*"))) { // we
																		// have
																		// arguments
			StringBuilder argBuilder = new StringBuilder();
			String next = s.contains("(") && s.indexOf("(") != s.length() - 1 ? s : sc.next();
			boolean foundClosingParen = false;
			do {
				if (next.contains("(")) {
					next = next.substring(next.indexOf("(") + 1);
				}
				if (next.contains(")")) {
					next = next.substring(0, next.indexOf(")"));
					foundClosingParen = true;
				}
				argBuilder.append(next);
				if (!foundClosingParen) {
					if (!sc.hasNext()) {
						throw new IllegalArgumentException("Unmatched parenthesis in statement \"" + statement + "\"");
					}
					argBuilder.append(" ");
					next = sc.next();
				}
			} while (!foundClosingParen);
			args = Stream.of(argBuilder.toString().replaceAll("\\(|\\)", "").split(",")).map(String::trim)
					.filter(arg -> !arg.equals("")).toArray(String[]::new);
		} else {
			args = new String[] {};
		}
		return args;
	}
	
	private String parseIndexString(Scanner scanner) throws PintoSyntaxException {
			StringBuilder sb = new StringBuilder();
			String next = null;
			do {
				if(!scanner.hasNext()) {
					throw new PintoSyntaxException("Missing \"]\" for index.");
				}
				next = scanner.next();
				sb.append(next);
			} while (!next.contains("]"));
			return sb.toString().replaceAll("\\[|\\]", "").replaceAll("\\s", "");
	}
	

	private static class Indexer {
		
		private boolean everything = true;
		private Integer start = null;
		private Integer end = null;
		private TreeSet<Integer> indicies = new TreeSet<>(Collections.reverseOrder());
		
		public static Indexer ALL = new Indexer();
		
		private Indexer() {}

		Indexer(String indexString, int stackSize) throws PintoSyntaxException {
			everything = false;
			if(indexString.contains(":") && indexString.contains(",")) {
				throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Cannot combine range indexing with multiple indexing.");
			} else if (!indexString.contains(":")) {
				Stream.of(indexString.split(",")).map(Integer::parseInt)
					.map(i -> i < 0 ? i + stackSize : i).forEach(indicies::add);
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
			
			if(start != null) {
				start = start < 0 ? start + stackSize : start;
				end = end < 0 ? end + stackSize : end;
				if (start > end) {
					throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Start is after end.");
				} else if (start < 0) {
					throw new PintoSyntaxException("Invalid index \"" + indexString + "\". Start is too low.");
				} else if (end >= stackSize) {
					throw new PintoSyntaxException("Invalid index \"" + indexString + "\". End too high for stack size.");
				}
			} else {
				for(int i : indicies) {
					if (i < 0) {
						throw new PintoSyntaxException("Invalid index \"" + i + "\". Start is too low.");
					} else if (i >= stackSize) {
						throw new PintoSyntaxException("Invalid index \"" + i + "\". End too high for stack size.");
					}
					
				}
			}
		}
		
		LinkedList<Function> index(LinkedList<Function> stack) throws PintoSyntaxException {
			LinkedList<Function> indexed = new LinkedList<>();
			if(everything) {
				indexed.addAll(stack);
				stack.clear();
			} else {
				if(start != null) {
					IntStream.range(start,end + 1).forEach(indicies::add);
				} 
				for(int i : indicies) {
					if(stack.size() == 0) {
						throw new PintoSyntaxException();
					}
					indexed.addLast(stack.remove(i));
				}
			}
			return indexed;
		}
	}

}
