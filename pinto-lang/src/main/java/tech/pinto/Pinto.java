package tech.pinto;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class Pinto {
	
	static private Pattern NAME_LITERAL = Pattern.compile(":(\\S+)");
	static private Pattern DATE_LITERAL = Pattern.compile("\\d{4}-[01]\\d-[0-3]\\d");
	static private Pattern ILLEGAL_NAME = Pattern.compile(".*[\\{\\}\\[\\]\"\\s:].*");
	private static int port;
	//private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Inject
	Namespace namespace;
	@Inject	
	MarketData marketdata;
	Expression activeExpression = new Expression(false);

	@Inject
	public Pinto() {
	}
	
	public List<Table> evaluate(String toEvaluate) {
		try {
			List<Expression> expressions =  parse(toEvaluate, activeExpression);
			if(expressions.size() > 0 && !expressions.get(expressions.size()-1).hasTerminal()) {
				activeExpression = expressions.remove(expressions.size()-1);
			} else {
				activeExpression = new Expression(false);
			}
			return expressions.stream().map(Expression::evaluate).collect(Collectors.toList());
		} catch(RuntimeException t) {
			activeExpression = new Expression(false);
			throw t;
		}
	}

	public Expression parseSubExpression(String expression) {
		return parse(expression, new Expression(true)).get(0);
	}

	public List<Expression> parse(String toParse, Expression e) {
		List<Expression> responses = new ArrayList<>();
		toParse = toParse.replaceAll("\\(", " \\( ").replaceAll("\\)", " \\) ");
		e.addText(toParse);
		try (Scanner sc = new Scanner(toParse)) {
			while (sc.hasNext()) {
				if(sc.hasNext(Pattern.compile("#.*"))) { // comment
					sc.nextLine();
				} else if(e.isExpressionStart() && sc.hasNext(NAME_LITERAL)) { // name literal
					e.setNameLiteral(checkName(sc.next(NAME_LITERAL).replaceAll(":", "")));
				} else if (sc.hasNext(Pattern.compile("\\(.*?"))) { // add inline function
					e.addFunction(parseSubExpression(parseBlock(sc,"\\(","\\)")));
				} else if (sc.hasNext(Pattern.compile("\\[.*?"))) { // indexer
					e.addIndexer(new Indexer(this, e.getDependencies(), parseBlock(sc, "\\[", "\\]")));
				} else if (sc.hasNext(Pattern.compile("\\{.*?"))) { // header literal
					e.addFunction(toTableConsumer(new HeaderLiteral(this, e.getDependencies(), parseBlock(sc, "\\{", "\\}"))));
				} else if (sc.hasNext(Pattern.compile("\\$.*?"))) { // market literal
					e.addFunction(toTableConsumer(marketdata.getStackFunction(parseBlock(sc,"\\$","\\$"))));
				} else if (sc.hasNext(Pattern.compile("\".*?"))) { // string literal
					e.addFunction(toTableConsumer(new tech.pinto.Column.OfConstantStrings(parseBlock(sc,"\"","\""))));
				} else if (sc.hasNextDouble()) { // double literal
					e.addFunction(toTableConsumer(new tech.pinto.Column.OfConstantDoubles(sc.nextDouble())));
				} else if (sc.hasNext(DATE_LITERAL)) { // date literal
					e.addFunction(toTableConsumer(new tech.pinto.Column.OfConstantDates(LocalDate.parse(sc.next(DATE_LITERAL)))));
				} else { // name
					String n = sc.next();
					Consumer<Table> c = t -> {
						Name name = namespace.getName(n);
						name.getIndexer(this).accept(t);
						name.getFunction().accept(this, t);
					};
					Name name = namespace.getName(n);
					if(!name.isTerminal()) {
						if(!name.isBuiltIn()) {
							e.getDependencies().add(name.toString());
						}
						e.addFunction(c);
					} else {
						e.addTerminal(c, name.isSkipEvaluation());
						responses.add(e);
						e = new Expression(false);
					}
				}
			}
			responses.add(e);
			return responses;
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
	
	public static Consumer<Table> toTableConsumer(Column<?> col) {
		return toTableConsumer(s -> s.addFirst(col));
	}

	public static Consumer<Table> toTableConsumer(Consumer<LinkedList<Column<?>>> colsFunction) {
		return t -> {
			List<LinkedList<Column<?>>> stacksBefore = t.takeTop();
			for(LinkedList<Column<?>> s : stacksBefore) {
				colsFunction.accept(s);
			}
			t.insertAtTop(stacksBefore);
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
				List<LinkedList<Column<?>>> inputs = t.takeTop();
				for(int i = 0; i < inputs.size(); i++) {
					accept(p, inputs.get(i));
				}
				t.insertAtTop(inputs);
			};
			
		}
	}
	
	public static class Expression implements Consumer<Table> {
		
		private final boolean isSubExpression;
		private boolean isNullary = false;
		private ArrayList<Consumer<Table>>  functions = new ArrayList<>();
		private Set<String> dependencies = new HashSet<>();
		private StringBuilder text = new StringBuilder();
		private	Optional<String> nameLiteral = Optional.empty();
		private	Optional<Consumer<Table>> terminalFunction = Optional.empty();
		private boolean skipEvaluation;
		
		public Expression(boolean isSubExpression) {
			this.isSubExpression = isSubExpression;
		}

		public Table evaluate() {
			Consumer<Table> f = terminalFunction.orElseThrow(() -> new PintoSyntaxException("Need terminal to evaluate expression."));
			Table t = new Table();
			if(!skipEvaluation) {
				accept(t);
			}
			f.accept(t);
			return t;
		}

		@Override
		public void accept(Table t) {
			for(int i = 0; i < functions.size(); i++) {
				functions.get(i).accept(t);
			}
			t.collapseFunction();
		}
		
		public void addIndexer(Indexer indexer) {
			if(functions.isEmpty()) {
				isNullary = indexer.isNone();
				indexer.setIndexForExpression();
			}
			functions.add(indexer);
		}

		public void addFunction(Consumer<Table> function) {
			if(functions.isEmpty() && !isSubExpression) {
				isNullary = true;
				Indexer i = new Indexer();
				i.setIndexForExpression();
				functions.add(i);
			}
			functions.add(function);
		}

		public void addTerminal(Consumer<Table> function, boolean skipEvaluation) {
			if(isSubExpression) {
				throw new PintoSyntaxException("Sub-expressions cannot include terminal functions");
			}
			this.terminalFunction = Optional.of(function);
			this.skipEvaluation = skipEvaluation;
		}

		public ArrayList<Consumer<Table>> getFunctions() {
			return functions;
		}
		
		public boolean isExpressionStart() {
			return functions.size() == 0;
		}

		public boolean isNullary() {
			return isNullary;
		}

		public Set<String> getDependencies() {
			return dependencies;
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
		
		public boolean hasTerminal() {
			return terminalFunction.isPresent();
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
