package tech.pinto.command.anyany;

import java.util.ArrayDeque;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Cache;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Vocabulary;
import tech.pinto.command.Command;
import tech.pinto.command.nonedouble.Literal;
import tech.pinto.command.terminal.Save;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Statement extends Command<Object, AnyData, Object, AnyData> {

	private ArrayDeque<Command<?, ?, ?, ?>> stack;
	private final ArrayDeque<Command<?, ?, ?, ?>> terminalCommands = new ArrayDeque<>();
	private final Set<String> dependencies = new HashSet<>();

	public Statement(Cache cache, Vocabulary vocab, String statement) throws PintoSyntaxException {
		this(cache, vocab, statement, new ArrayDeque<>());

	}

	public Statement(Cache cache, Vocabulary vocab, String statement,
			ArrayDeque<Command<?, ?, ?, ?>> stack) throws PintoSyntaxException {
		super("statement", AnyData.class, AnyData.class);
		List<String> sb = new ArrayList<>();
		this.stack = stack;
		try (Scanner sc = new Scanner(statement)) {
			while (sc.hasNext()) {
				if (sc.hasNextDouble()) { // change to literal
					stack.addFirst(new Literal(sc.nextDouble()));
					sb.add(stack.peekFirst().toString());
					continue;
				}
				String s = sc.next();
				Command<?, ?, ?, ?> c = null;
				String commandName = s.contains("(") ? s.substring(0, s.indexOf("(")) : s;
				if (vocab.commandExists(commandName)) { // it's a known command
					try {
						c = vocab.getCommand(commandName, cache, parseCommandArguments(statement, s, sc));
					} catch(IllegalArgumentException e) {
						throw new PintoSyntaxException("Wrong arguments for command: " + commandName);
					}
					int inputsRequired = c.inputCount();
					boolean unlimitedInputs = inputsRequired == Integer.MAX_VALUE;
					while (stack.size() > 0 && (inputsRequired > 0 || unlimitedInputs)) {
						if (c.getInputType().equals(AnyData.class)
								|| stack.peekFirst().getInputType().equals(AnyData.class)
								|| c.getInputType().equals(stack.peekFirst().getOutputType())) {
							if(unlimitedInputs || inputsRequired >= stack.peekFirst().outputCount()) {
								// we need all of this command's outputs
								inputsRequired -= stack.peekFirst().outputCount();
								c.getInputCommands().add((Command) stack.removeFirst());
							} else {
								c.getInputCommands().add((Command) stack.peekFirst());
								stack.peekFirst().decrementOutputCountBy(inputsRequired);
								inputsRequired = 0;
							}
						} else {
							throw new PintoSyntaxException("Incorrect type on stack for \"" + c.toString() + "\"."
									+ " (Needed: \"" + c.getInputType().getName() + "\", On stack: \""
									+ stack.peekFirst().getInputType().getName() + "\")");
						}
					}
//					if(inputsRequired > 0 && ! unlimitedInputs) {
//						throw new PintoSyntaxException(
//							"Insufficient inputs on stack for \"" + c.toString() + "\": " +
//								c.inputCount() + "required.");
//					}
					if (c.isTerminal()) {
						terminalCommands.addFirst(c);
						if( c instanceof Save) {
							((Save) c).setFormula(joinWithSpaces(sb));
						}
					}
					stack.addFirst(c);
					sb.add(c.toString());
				} else { // it's the name of a saved statement (hopefully)
					if (!cache.isSavedStatement(s)) {
						throw new PintoSyntaxException("Command or saved statement \"" + s + "\" not found.");
					}
					dependencies.add(s);
					c = new Statement(cache, vocab, cache.getSaved(s), stack);
					sb.add(s);
				}
			}
		}
		outputCount = stack.stream().mapToInt(Command::outputCount).sum();
	}

	public ArrayDeque<Command<?, ?, ?, ?>> getStack() {
		return stack;
	}

	@SuppressWarnings("unchecked")
	public <P extends Period> ArrayDeque<AnyData> evaluate(PeriodicRange<P> range) {
		// if range is null this is a top-level statement. we need to find a
		// terminal command to start recursively executing

		if (range == null) {
			return terminalCommands.stream().flatMap(c -> c.getOutputData(null).stream())
					.map(Data.class::cast).collect(Collectors.toCollection(()-> new ArrayDeque()));
		} else {
			ArrayDeque<AnyData> output = new ArrayDeque<>();
			stack.stream().flatMap(c -> c.getOutputData(range).stream())
					.forEach(d -> output.addFirst((AnyData) d));
			return output;
		}

	}

	@Override
	public Set<String> getDependencies() {
		return dependencies;
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
					.toArray(String[]::new);
		} else {
			args = new String[] {};
		}
		return args;
	}

}
