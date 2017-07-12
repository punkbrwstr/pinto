package tech.pinto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.inject.Inject;

import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.header.HeaderLiteral;
import tech.pinto.function.intermediate.Head;
import tech.pinto.function.supplier.SeriesLiteral;

public class Pinto {

	@Inject
	Namespace namespace;
	//private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

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
						currentFunction = new SeriesLiteral(currentFunction, indexer, d);
						indexer = Indexer.ALL;
					} else if(sc.hasNext(Pattern.compile("\".*"))) {
						StringBuilder sb = new StringBuilder();
						do {
							if(!sc.hasNext()) {
								throw new PintoSyntaxException("Unmatched double quote in literal.");
							}
							sb.append(sc.next()).append(" ");
						} while(countOccurrences(sb.toString(), '"') < 2);
						currentFunction = new HeaderLiteral(currentFunction, indexer, sb.toString().replaceAll("\"", "").trim());
						
					} else {
						String s = sc.next();
						String commandName = s.contains("(") ? s.substring(0, s.indexOf("(")) : s;
						if (!namespace.contains(commandName)) {
							throw new PintoSyntaxException("Name not found: \"" + s + "\" in expression \"" + expression + "\"");
						}
						try {
							currentFunction = namespace.getFunction(commandName, this, currentFunction, indexer);
						} catch (IllegalArgumentException e) {
								throw new PintoSyntaxException("Wrong arguments for " + commandName + ": " + e.getMessage(), e);
						}
						indexer = Indexer.ALL;
					}
					if (currentFunction instanceof TerminalFunction) {
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
	
	public static int countOccurrences(String haystack, char needle) {
	    int count = 0;
	    for(int i=0; i < haystack.length(); i++) {
	        if (haystack.charAt(i) == needle) {
	             count++;
	        }
	    }
	    return count;
	}

}
