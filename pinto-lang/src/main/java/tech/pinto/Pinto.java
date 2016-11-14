package tech.pinto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.intermediate.Head;
import tech.pinto.function.supplier.Literal;

public class Pinto {

	@Inject
	Namespace namespace;
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

	@Inject
	public Pinto() {
	}

	public List<TerminalFunction> execute(String expression) throws Exception {
		List<TerminalFunction> output = new ArrayList<>();
		ComposableFunction currentFunction = null;
		Indexer indexer = Indexer.ALL;
		try (Scanner sc = new Scanner(expression)) {
			while (sc.hasNext()) {
				if (sc.hasNext(Pattern.compile("\\[.*"))) { // index
					indexer = new Indexer(parseIndexString(sc));
				} else {
					if(currentFunction == null) {
						currentFunction = new Head(indexer);
					}
					if (sc.hasNextDouble()) { // double literal
						final double d = sc.nextDouble();
						currentFunction = new Literal(currentFunction, indexer, d);
					} else {
						String s = sc.next();
						String commandName = s.contains("(") ? s.substring(0, s.indexOf("(")) : s;
						if (!namespace.contains(commandName)) {
							throw new PintoSyntaxException("Name not found: \"" + s + "\" in expression \"" + expression + "\"");
						}
						try {
							currentFunction = namespace.getFunction(commandName, this, currentFunction,
									indexer, parseCommandArguments(expression, s, sc));
						} catch (IllegalArgumentException e) {
								throw new PintoSyntaxException("Wrong arguments for " + commandName + ": " + e.getMessage(), e);
						}
						indexer = Indexer.ALL;
					}
					if (currentFunction instanceof TerminalFunction) {
						log.trace("Composing: {}", currentFunction.toExpressionTrace());
						output.add((TerminalFunction) currentFunction);
						currentFunction = null;
					}
				}

			}
			return output;
		} catch (RuntimeException e) {
			throw new Exception(e);
		}
	}

	public Namespace getNamespace() {
		return namespace;
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

}
