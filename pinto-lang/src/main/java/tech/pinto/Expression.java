package tech.pinto;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.function.Function;
import tech.pinto.function.LambdaFunction;
import tech.pinto.function.ReferenceFunction;
import tech.pinto.function.TerminalFunction;

public class Expression extends ReferenceFunction {

	private final ArrayDeque<TerminalFunction> terminalCommands = new ArrayDeque<>();
	private final Set<String> dependencies = new HashSet<>();

	public Expression(Namespace namespace, String text) throws PintoSyntaxException {
		this(namespace, text, new LinkedList<>());
	}

	public Expression(Namespace namespace, String text, LinkedList<Function> existingStack)
			throws PintoSyntaxException {
		super("statement", new LinkedList<>(existingStack));
		labeller = f -> text;
		existingStack.clear();
		Indexer indexer = Indexer.ALL;
		List<String> expressionToSave = new ArrayList<>();
		try (Scanner sc = new Scanner(text)) {
			while (sc.hasNext()) {
				if (sc.hasNext(Pattern.compile("\\[.*"))) { // index
					indexer = new Indexer(parseIndexString(sc), inputStack);
				} else {
					Function c = null;
					if (sc.hasNextDouble()) { // double literal
						final double d = sc.nextDouble();
						c = new LambdaFunction(f -> Double.toString(d),
								x -> range -> DoubleStream.iterate(d, r -> d).limit(range.size()));
					} else {
						String s = sc.next();
						String commandName = s.contains("(") ? s.substring(0, s.indexOf("(")) : s;
						if (!namespace.contains(commandName)) {
							throw new PintoSyntaxException("Name \"" + s + "\" not found.");
						}
						try {
							c = namespace.getFunction(commandName, 
										indexer.index(inputStack), new ArrayList<>(expressionToSave), parseCommandArguments(text, s, sc));
						} catch (IllegalArgumentException e) {
								throw new PintoSyntaxException("Wrong arguments for " + commandName + ": " + e.getMessage(), e);
						}
						indexer = Indexer.ALL;
					}
					expressionToSave.add(c.toString());
					if (c instanceof TerminalFunction) {
						terminalCommands.addFirst((TerminalFunction) c);
						expressionToSave.clear();
					} else {
						int count = c.getOutputCount();
						for (int i = 0; i < count; i++) {
							inputStack.addFirst(c.getReference());
						}
					}
				}
			}
		}
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

	@Override
	public int getOutputCount() {
		return inputStack.size();
	}

}
