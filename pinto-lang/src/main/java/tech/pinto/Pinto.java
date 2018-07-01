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
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Inject
	Namespace namespace;
	@Inject	
	MarketData marketdata;
	Expression activeExpression = new Expression(false);

	@Inject
	public Pinto() {
	}

	public List<Table> eval(String toEvaluate) {
		return evaluate(toEvaluate, activeExpression);
	}

	public LinkedList<Column<?>> parseSubExpression(String expression) {
		Table t = evaluate(expression, new Expression(true)).get(0);
		return t.flatten();
	}

	public List<Table> evaluate(String toEvaluate, Expression e) {
		List<Table> responses = new ArrayList<>();
		toEvaluate = toEvaluate.replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ");
		e.addText(toEvaluate);
		try (Scanner sc = new Scanner(toEvaluate)) {
			while (sc.hasNext()) {
				if(sc.hasNext(Pattern.compile("#.*"))) { // comment
					sc.nextLine();
				} else if(e.isExpressionStart() && sc.hasNext(NAME_LITERAL)) { // name literal
					String nl = sc.next(NAME_LITERAL).replaceAll(":", "");
					e.setNameLiteral(checkName(nl));
					String functionIndexString = sc.hasNext(INDEXER) ? sc.next(INDEXER) : "[:]";
					e.setDefinedIndexer(new Indexer(functionIndexString.replaceAll("^\\[|\\]$", ""), true));
				} else if(e.isInlineStart()) {
					String functionIndexString = sc.hasNext(INDEXER) ? sc.next(INDEXER) : "[:]";
					e.setCurrent(e.getCurrent().andThen(
							new Indexer(functionIndexString.replaceAll("^\\[|\\]$", ""), true).apply(this)));
				} else if (sc.hasNext(Pattern.compile(".*?\\)"))) { // end inline function
					sc.next();
					e.setCurrent(e.getCurrent().andThen(t2 -> {
						t2.collapseFunction();
					}));
				} else if (sc.hasNext(Pattern.compile("\\(.*?"))) { // start inline function
					sc.next();
					e.startInline();
				} else if (sc.hasNextDouble()) { // double literal
					double d = sc.nextDouble();
					e.setCurrent(e.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantDoubles(d));
					})));
				} else if (sc.hasNext(DATE_LITERAL)) { // date literal
					final LocalDate d = LocalDate.parse(sc.next(DATE_LITERAL));
					e.setCurrent(e.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantDates(d));
					})));
				} else if (sc.hasNext(Pattern.compile("\\[.*?"))) { // indexer
					e.setCurrent(e.getCurrent().andThen(
							new Indexer(parseBlock(sc, "\\[", "\\]"), false).apply(this)));
				} else if (sc.hasNext(Pattern.compile("\\{.*?"))) { // header literal
					e.setCurrent(e.getCurrent().andThen(toTableConsumer(
							new HeaderLiteral(this, parseBlock(sc, "\\{", "\\}")))));
				} else if (sc.hasNext(Pattern.compile("\".*?"))) { // string literal
					final String sl = parseBlock(sc,"\"","\"");
					e.setCurrent(e.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantStrings(sl));
					})));
				} else if (sc.hasNext(Pattern.compile("\\$.*?"))) { // market literal
					final String sl = parseBlock(sc,"\\$","\\$");
					e.setCurrent(e.getCurrent().andThen(toTableConsumer(s -> {
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
							s.addFirst(new Column.OfDoubles(inputs -> tickersfields.get(index),
									(range, inputs) -> Cache.getCachedValues(key, range, index, tickersfields.size(),
												marketdata.getFunction(tickers, fields))));
						}
					})));
				} else { // name
					String name = sc.next();
					if (!namespace.contains(name)) {
						throw new PintoSyntaxException("Name \"" + name + "\" not found in \"" + toEvaluate + "\"");
					}
					e.getDependencies().add(name);
					Name n = namespace.getName(name);
					e.setCurrent(e.getCurrent().andThen(t -> {
						namespace.getName(name).getDefaultIndexer(this).accept(t);
					}));
					e.setCurrent(e.getCurrent().andThen(t -> {
						namespace.getName(name).apply(this).accept(t);
					}));
					if(n.terminal()) {
						if(e.isSubExpression()) {
							throw new PintoSyntaxException("Sub-expressions cannot include terminal functions");
						}
						Table t = new Table();
						if(!n.startFromLast()) {
							e.getCurrent().accept(t); 
						} else {
							namespace.getName(name).apply(this).accept(t); // used by def
						}
						log.info("expression: {}", e.getText());
						responses.add(t);
						e.reset();
					}
				}
			}
			if(e.isSubExpression()) {
				Table t = new Table();
				e.getCurrent().accept(t);
				responses.add(t);
			} 
			return responses;
		} catch (RuntimeException re) {
			e.reset();
			throw re;
		}
	}
	
	public Namespace getNamespace() {
		return namespace;
	}
	
	public Expression getExpression() {
		return activeExpression;
	}
	
	public void setPort(int port) {
		Pinto.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public static Consumer<Table> toTableConsumer(Consumer<LinkedList<Column<?>>> colsFunction) {
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
	
	@FunctionalInterface
	public static interface TableFunction {
		public void accept(Pinto pinto, Table table);
	}

	@FunctionalInterface
	public static interface StackFunction {
		public void accept(Pinto pinto, LinkedList<Column<?>> stack);
		default TableFunction toTableFunction() {
			return (p,t) -> {
				t.insertAtTop(t.takeTop().stream().map(c -> { 
					accept(p,c);	
					return c;
				}).collect(Collectors.toList()));
			};
			
		}
	}
	
	public static class Expression {
		
		private final boolean isSubExpression;
		private boolean expressionStart = true;
		private boolean inlineStart = false;
		private Consumer<Table> current = t -> {};
		private Consumer<Table> previous = t -> {};
		private List<String> dependencies = new ArrayList<>();
		private StringBuilder text = new StringBuilder();
		private	Optional<String> nameLiteral = Optional.empty();
		private Indexer definedIndexer = Indexer.ALL_DEFINED;
		
		public Expression(boolean isSubExpression) {
			this.isSubExpression = isSubExpression;
		}

		public void reset() {
			expressionStart = true;
			current = t -> {};
			previous = t -> {};
			dependencies = new ArrayList<>();
			text = new StringBuilder();
			nameLiteral = Optional.empty();
			definedIndexer = Indexer.ALL_DEFINED;
			inlineStart = false;
		}
		
		public void startInline() {
			inlineStart = true;
		}
		
		public boolean isInlineStart() {
			boolean b = inlineStart;
			inlineStart = false;
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
		public String getText() {
			return text.toString();
		}
		public void addText(String expression) {
			this.text.append(" ").append(expression);
		}
		public Optional<String> getNameLiteral() {
			return nameLiteral;
		}
		public void setNameLiteral(String nameLiteral) {
			this.nameLiteral = Optional.of(nameLiteral);
		}
		public Indexer getDefinedIndexer() {
			return definedIndexer;
		}
		public void setDefinedIndexer(Indexer nameIndexer) {
			this.definedIndexer = nameIndexer;
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
