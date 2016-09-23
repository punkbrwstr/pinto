package tech.pinto.command.anyany;

import java.util.ArrayDeque;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import tech.pinto.Cache;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Vocabulary;
import tech.pinto.command.Command;
import tech.pinto.command.SimpleCommand;
import tech.pinto.command.nonedouble.Literal;
import tech.pinto.command.terminal.Save;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Statement extends Command {

	private final ArrayDeque<Command> terminalCommands = new ArrayDeque<>();
	private final List<Command> inputsToStatement = new ArrayList<>();
	private final Set<String> dependencies = new HashSet<>();
	private final String text;
	private final boolean isNested;

	public Statement(Cache cache, Vocabulary vocab, String text, boolean isNested) throws PintoSyntaxException {
		super("statement", AnyData.class, AnyData.class);
		this.text = text;
		this.isNested = isNested;
		inputCount = 0;
		List<String> sb = new ArrayList<>();
		try (Scanner sc = new Scanner(text)) {
			while (sc.hasNext()) {
				if (sc.hasNextDouble()) { // change to literal
					inputStack.addFirst(new Literal(sc.nextDouble()));
					sb.add(inputStack.peekFirst().toString());
					continue;
				}
				String s = sc.next();
				Command c = null;
				String commandName = s.contains("(") ? s.substring(0, s.indexOf("(")) : s;
				if (vocab.commandExists(commandName)) { // it's a known command
					try {
						c = vocab.getCommand(commandName, cache, parseCommandArguments(text, s, sc));
					} catch (IllegalArgumentException e) {
						throw new PintoSyntaxException("Wrong arguments for " + commandName + ": " + e.getMessage(),e);
					}
					if (c.isTerminal()) {
						terminalCommands.addFirst(c);
						if (c instanceof Save) {
							((Save) c).setFormula(joinWithSpaces(sb));
						}
					} else {
						sb.add(c.toString());
					}
				} else { // it's the name of a saved statement (hopefully)
					if (!cache.isSavedStatement(s)) {
						throw new PintoSyntaxException("Command or saved statement \"" + s + "\" not found.");
					}
					dependencies.add(s);
					c = new Statement(cache, vocab, cache.getSaved(s), true);
					sb.add(s);
				}
				while (inputStack.size() > 0 && c.inputsNeeded() > 0) {
					if (!(c.getInputType().equals(AnyData.class)
							|| inputStack.peekFirst().getInputType().equals(AnyData.class)
							|| c.getInputType().equals(inputStack.peekFirst().getOutputType()))) {
						throw new PintoSyntaxException("Incorrect type on stack for \"" + c.toString() + "\"."
								+ " (Needed: \"" + c.getInputType().getName() + "\", On stack: \""
								+ inputStack.peekFirst().getInputType().getName() + "\")");
					}
					c.addInput(inputStack.removeFirst());
					inputCount += c.inputsNeeded();
				}
				while (inputStack.size() == 0 && c.inputsNeeded() > 0 && c.inputsNeeded() != Integer.MAX_VALUE) {
					if(!isNested) {
						throw new PintoSyntaxException("Insufficient inputs for " + c.toString() + ". " 
									+ c.inputsNeeded() + " more required.");
					}
					final int i = inputCount++;
					c.addInput(new SimpleCommand(this,1,1,range -> inputsToStatement.get(i).evaluate(range)));
				}
				for (int i = 0; i < c.outputCount(); i++) {
					inputStack.addFirst(c.getReference());
				}
			}
		}
		outputCount = inputStack.size();
	}
	
	@Override
	public int inputsNeeded() {
		return inputCount - inputsToStatement.size();
	}
	
	@Override
    public void addInput(Command c) {
		inputsToStatement.add(c);
    }

	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		return inputStack.removeFirst().evaluate(range);
	}

	@Override
	public Set<String> getDependencies() {
		return dependencies;
	}
	
	public ArrayDeque<Command> getTerminalCommands() {
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

}
