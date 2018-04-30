package tech.pinto;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class Pinto {
	
	static Pattern STRING_LITERAL = Pattern.compile("\"(.*?)\"");
	static Pattern INDEXER = Pattern.compile("\\[(.*?)\\]");
	static Pattern HEADER_LITERAL = Pattern.compile("\\{(.*)\\}");
	static Pattern NAME_LITERAL = Pattern.compile(":(\\S+)");
	static Pattern DATE_LITERAL = Pattern.compile("\\d{4}-[01]\\d-[0-3]\\d");
	static Pattern ILLEGAL_NAME = Pattern.compile(".*[\\{\\}\\[\\]\"\\s:].*");
	private static int port;

	@Inject
	Namespace namespace;
	@Inject	
	MarketData marketdata;
	State engineState = new State(false);

	@Inject
	public Pinto() {
	}

	public List<Table> eval(String expression) {
		return evaluate(expression, engineState);
	}

	public LinkedList<Column<?,?>> parseSubExpression(String expression) {
		Table t = evaluate(expression, new State(true)).get(0);
		return t.flatten();
	}

	public List<Table> evaluate(String expression, State state) {
		List<Table> responses = new ArrayList<>();
		expression = expression.replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ");
		try (Scanner sc = new Scanner(expression)) {
			while (sc.hasNext()) {
				if(state.isExpressionStart()) {
					if(sc.hasNext(NAME_LITERAL)) { // name literal
						String nl = sc.next(NAME_LITERAL).replaceAll(":", "");
						state.setNameLiteral(checkName(nl));
						String functionIndexString = sc.hasNext(INDEXER) ? sc.next(INDEXER) : "[:]";
						state.setNameIndexer(new Indexer(this, functionIndexString.replaceAll("^\\[|\\]$", ""), true));
						state.setExpression(expression);
					} else if(sc.hasNext(Pattern.compile("#.*"))) { // comment
						sc.nextLine();
						state.reset();
						continue;
					}
				}
				if(state.isInlineStart()) {
					String functionIndexString = sc.hasNext(INDEXER) ? sc.next(INDEXER) : "[:]";
					state.setCurrent(state.getCurrent().andThen(
							new Indexer(this, functionIndexString.replaceAll("^\\[|\\]$", ""), true)));
				}
				if (sc.hasNext(Pattern.compile(".*?\\)"))) { // end inline function
					if(!state.isInline()) {
						throw new PintoSyntaxException("Closing inline function that had not been opened in \"" + expression + "\"");
					}
					sc.next();
					state.setCurrent(state.getCurrent().andThen(t2 -> {
						t2.collapseFunction();
					}));
				}
				if (sc.hasNext(Pattern.compile("\\(.*?"))) { // start inline function
					sc.next();
					state.startInline();
				} else if (sc.hasNextDouble()) { // double literal
					double d = sc.nextDouble();
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantDoubles(d));
					})));
				} else if (sc.hasNext(DATE_LITERAL)) { // date literal
					final LocalDate d = LocalDate.parse(sc.next(DATE_LITERAL));
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantDates(d));
					})));
				} else if (sc.hasNext(Pattern.compile("\\[.*?"))) { // indexer
					state.setCurrent(state.getCurrent().andThen(
							new Indexer(this, parseBlock(sc, "\\[", "\\]"), false)));
				} else if (sc.hasNext(Pattern.compile("\\{.*?"))) { // header literal
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(
							new HeaderLiteral(this, parseBlock(sc, "\\{", "\\}")))));
				} else if (sc.hasNext(Pattern.compile("\".*?"))) { // string literal
					final String sl = parseBlock(sc,"\"","\"");
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantStrings(sl,sl));
					})));
				} else if (sc.hasNext(Pattern.compile("\\$.*?"))) { // market literal
					final String sl = parseBlock(sc,"\\$","\\$");
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(s -> {
						String[] sa = sl.split(":");
						List<String> tickers = Arrays.asList(sa[0].split(","));
						String fieldsString = sa.length > 1 ? sa[1] : "PX_LAST";
						List<String> fields = Arrays.stream(fieldsString.split(",")).map(f -> f.trim()).map(String::toUpperCase)
								.map(f -> f.replaceAll(" ", "_")).collect(Collectors.toList());
						String key = tickers.stream().collect(Collectors.joining(",")) + fields.stream().collect(Collectors.joining(","));
						List<String> tickersfields = tickers.stream().flatMap(t -> fields.stream().map(c -> t + ":" + c))
								.collect(Collectors.toList());
						for (int i = 0; i < tickersfields.size(); i++) {
							final int index = i;
							s.addFirst(new Column.OfDoubles(inputs -> tickersfields.get(index), inputs -> range -> {
								return Cache.getCachedValues(key, range, index, tickersfields.size(),
										marketdata.getFunction(tickers, fields));
							}));
						}
					})));
				} else { // name
					String name = sc.next();
					if (!namespace.contains(name)) {
						throw new PintoSyntaxException("Name \"" + name + "\" not found in \"" + expression + "\"");
					}
					state.getDependencies().add(name);
					Name n = namespace.getName(name);
					if(n.hasDefaultIndexer()) {
						state.setCurrent(state.getCurrent().andThen(t -> {
							namespace.getName(name).getDefaultIndexer(this).accept(t);
						}));
					}
					state.setCurrent(state.getCurrent().andThen(t -> {
						namespace.getName(name).apply(this).accept(t);
					}));
					if(n.terminal()) {
						if(state.isSubExpression()) {
							throw new PintoSyntaxException("Sub-expressions cannot include terminal functions");
						}
						Table t = new Table();
						if(!n.startFromLast()) {
							state.getCurrent().accept(t); 
						} else {
							namespace.getName(name).apply(this).accept(t); // used by def
						}
						responses.add(t);
						state.reset();
					}
				}
			}
			if(state.isSubExpression()) {
				Table t = new Table();
				state.getCurrent().accept(t);
				responses.add(t);
			} 
			return responses;
		} catch (RuntimeException e) {
			state.reset();
			throw e;
		}
	}
	
	public Namespace getNamespace() {
		return namespace;
	}
	
	public State getState() {
		return engineState;
	}
	
	public void setPort(int port) {
		Pinto.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public static Consumer<Table> toTableConsumer(Consumer<LinkedList<Column<?,?>>> colsFunction) {
		return t -> {
			t.insertAtTop(t.takeTop().stream().map(c -> { 
				colsFunction.accept(c);	
				return c;
			}).collect(Collectors.toList()));
		};
	}

	private static String checkName(String name) {
		if(ILLEGAL_NAME.matcher(name).matches()) {
			throw new PintoSyntaxException("Illegal character in name literal \"" + name + "\"");
		}
		return name;
	}
	
	public static class State {
		
		private final boolean isSubExpression;
		private boolean expressionStart = true;
		private boolean inline = false;
		private boolean inlineStart = false;
		private Consumer<Table> current = t -> {};
		private Consumer<Table> previous = t -> {};
		private List<String> dependencies = new ArrayList<>();
		private Optional<String> expression = Optional.empty();
		private	Optional<String> nameLiteral = Optional.empty();
		private Optional<Indexer> nameIndexer = Optional.empty();
		
		public State(boolean isSubExpression) {
			this.isSubExpression = isSubExpression;
		}

		public void reset() {
			expressionStart = true;
			current = t -> {};
			previous = t -> {};
			dependencies = new ArrayList<>();
			expression = Optional.empty();
			nameLiteral = Optional.empty();
			nameIndexer = Optional.empty();
			inline = false;
			inlineStart = false;
		}
		
		public void startInline() {
			inline = true;
			inlineStart = true;
		}
		
		public boolean isInlineStart() {
			boolean b = inlineStart;
			inlineStart = false;
			return b;
		}
		
		public boolean isInline() {
			boolean b = inline;
			inline = false;
			return b;
		}
		
		public boolean isExpressionStart() {
			boolean b = expressionStart;
			expressionStart = false;
			return b;
		}
		
		
		public boolean isSubExpression() {
			return isSubExpression;
		}

		public Consumer<Table> getCurrent() {
			return current;
		}

		public Consumer<Table> getPrevious() {
			return previous;
		}

		public void setCurrent(Consumer<Table> current) {
			this.previous = this.current;
			this.current = current;
		}
		public List<String> getDependencies() {
			return dependencies;
		}
		public void setDependencies(List<String> dependencies) {
			this.dependencies = dependencies;
		}
		public Optional<String> getExpression() {
			return expression;
		}
		public void setExpression(String expression) {
			this.expression = Optional.of(expression);
		}
		public Optional<String> getNameLiteral() {
			return nameLiteral;
		}
		public void setNameLiteral(String nameLiteral) {
			this.nameLiteral = Optional.of(nameLiteral);
		}
		public Optional<Indexer> getNameIndexer() {
			return nameIndexer;
		}
		public void setNameIndexer(Indexer nameIndexer) {
			this.nameIndexer = Optional.of(nameIndexer);
		}
	}
	
	
	public static String parseBlock(Scanner scanner, String opening, String closing) throws PintoSyntaxException {
		StringBuilder sb = new StringBuilder();
		String next = null;
		Pattern openPattern = Pattern.compile(".*?" + opening + ".*?");
		Pattern closePattern = Pattern.compile(".*?" + closing + ".*?");
		boolean sameOpenClose = opening.equals(closing);
		int openCount = sameOpenClose ? 2 : 0;
		do {
			if (!scanner.hasNext()) {
				throw new PintoSyntaxException("Missing " + closing);
			}
			next = scanner.next();
			sb.append(next).append(" ");
			Matcher openMatcher = openPattern.matcher(next);
			while(openMatcher.find() && ! sameOpenClose) {
				openCount++;
			}
			Matcher closeMatcher = closePattern.matcher(next);
			while(closeMatcher.find()) {
				openCount--;
			}
		} while (openCount != 0);
		return sb.toString().replaceAll("^" + opening + "|" + closing + " $" , "");
	}
	
}
