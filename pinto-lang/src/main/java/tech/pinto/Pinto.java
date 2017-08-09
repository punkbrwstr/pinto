package tech.pinto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.inject.Inject;

import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.functions.Constant;
import tech.pinto.function.functions.HeaderLiteral;

public class Pinto {

	@Inject
	Namespace namespace;
	// private final org.slf4j.Logger log =
	// org.slf4j.LoggerFactory.getLogger(getClass());
	private ComposableFunction currentFunction = null;

	@Inject
	public Pinto() {
	}

	public List<Table> execute(String expression) throws Exception {
		List<Table> output = new ArrayList<>();
		Indexer indexer = Indexer.ALL;
		try (Scanner sc = new Scanner(expression)) {
			while (sc.hasNext()) {
				if (sc.hasNext(Pattern.compile("\\[.*"))) { // indexer
					indexer = new Indexer(parseIndexString(sc));
				} else {
					if (currentFunction == null) {
						currentFunction = new ComposableFunction(indexer);
						indexer = Indexer.ALL;
					}
					if (sc.hasNextDouble()) { // double literal
						final double d = sc.nextDouble();
						currentFunction = new Constant(currentFunction, indexer, d);
						indexer = Indexer.ALL;
					} else if (sc.hasNext(Pattern.compile("\".*"))) { // string literal
						StringBuilder sb = new StringBuilder();
						do {
							if (!sc.hasNext()) {
								throw new PintoSyntaxException("Unmatched double quote in literal.");
							}
							sb.append(sc.next()).append(" ");
						} while (countOccurrences(sb.toString(), '"') < 2);
						currentFunction = new HeaderLiteral(currentFunction, indexer,
								sb.toString().replaceAll("\"", "").trim());
						// keep indexer for next function
					} else { // function
						String name = sc.next();
						if (!namespace.contains(name)) {
							throw new PintoSyntaxException(
									"Name not found: \"" + name + "\" in expression \"" + expression + "\"");
						}
						try {
							currentFunction = namespace.getFunction(name, this, currentFunction, indexer);
						} catch (IllegalArgumentException e) {
							throw new PintoSyntaxException("Wrong arguments for " + name + ": " + e.getMessage(), e);
						}
						indexer = Indexer.ALL;
					}
					if (currentFunction instanceof TerminalFunction) {
						output.add(((TerminalFunction) currentFunction).getTable());
						currentFunction = null;
					}
				}

			}
			return output;
		} catch (RuntimeException e) {
			currentFunction = null;
			throw new Exception(e);
		}
	}

	public Namespace getNamespace() {
		return namespace;
	}

	private String parseIndexString(Scanner scanner) throws PintoSyntaxException {
		StringBuilder sb = new StringBuilder();
		String next = null;
		do {
			if (!scanner.hasNext()) {
				throw new PintoSyntaxException("Missing \"]\" for index.");
			}
			next = scanner.next();
			sb.append(next);
		} while (!next.contains("]"));
		return sb.toString();
	}

	public static int countOccurrences(String haystack, char needle) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				count++;
			}
		}
		return count;
	}

}
