package tech.pinto;

import java.time.Duration;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import tech.pinto.time.PeriodicRange;
import static tech.pinto.tools.StringUtils.*;


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
	private Expression activeExpression = new Expression(false);
	private final Object activeExpressionLock = new Object();

	@Inject
	public Pinto() {
	}
	
	public List<Table> evaluate(String toEvaluate) {
		synchronized(activeExpressionLock) {
			try {
				List<Expression> expressions =  parse(toEvaluate, activeExpression);
				Expression next = expressions.size() > 0 
						&& !expressions.get(expressions.size()-1).hasTerminal() ?
								expressions.remove(expressions.size()-1) : new Expression(false);
				List<Table> tables =  new ArrayList<>();
				for(int i = 0; i < expressions.size(); i++) {
					tables.add(expressions.get(i).evaluate(this));
				}
				activeExpression = next;
				return tables;
			} catch(RuntimeException t) {
				activeExpression = new Expression(false);
				throw t;
			}
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
					e.addFunction(new HeaderLiteral(this, e.getDependencies(), parseBlock(sc, "\\{", "\\}")));
				} else if (sc.hasNext(Pattern.compile("\\$.*?"))) { // market literal
					e.addFunction(marketdata.getStackFunction(parseBlock(sc,"\\$","\\$")));
				} else if (sc.hasNext(Pattern.compile("\".*?"))) { // string literal
					e.addFunction(new Column<String>(String.class, "string", parseBlock(sc,"\"","\"")));
				} else if (sc.hasNextDouble()) { // double literal
					e.addFunction(new Column<Double>(Double.class, "c", sc.nextDouble()));
				} else if (sc.hasNext(DATE_LITERAL)) { // date literal
					e.addFunction(new Column<LocalDate>(LocalDate.class, "date", LocalDate.parse(sc.next(DATE_LITERAL))));
				} else { // name
					String n = sc.next();
					e.addFunction((p, t) -> {
						namespace.getName(n).getIndexer(this).accept(p, t);
					});
					Name name = namespace.getName(n);
					if(!name.isTerminal()) {
						if(!name.isBuiltIn()) {
							e.getDependencies().add(name.toString());
						}
						e.addFunction((p,t) -> {
							namespace.getName(n).getTableFunction().accept(p, t);
						});
					} else {
						e.setTerminal(name.getTerminalFunction());
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
	
	public void setPort(int port) {
		Pinto.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	private static String checkName(String name) {
		if(ILLEGAL_NAME.matcher(name).matches()) {
			throw new PintoSyntaxException("Illegal character in name literal \"" + name + "\"");
		}
		return name;
	}
	
	@FunctionalInterface
	public static interface TerminalFunction extends BiFunction<Pinto,Expression,Table> {}

	@FunctionalInterface
	public static interface TableFunction extends BiConsumer<Pinto,Table> {}

	@FunctionalInterface
	public static interface StackFunction extends TableFunction {
		void accept(Pinto p, Stack s);

		default void accept(Pinto p, Table t) {
				List<Stack> inputs = t.takeTop();
				for(int i = 0; i < inputs.size(); i++) {
					accept(p, inputs.get(i));
				}
				t.insertAtTop(inputs);
		}
	}
	
	public static class Stack extends LinkedList<Column<?>> {
		private static final long serialVersionUID = 1L;

		public Stack() {
			super();
		}

		public Stack(Collection<? extends Column<?>> c) {
			super(c);
		}
		
	}
	
	public static class Expression implements TableFunction {
		
		private final boolean isSubExpression;
		private ArrayList<TableFunction>  functions = new ArrayList<>();
		private	Optional<Indexer> indexer = null;
		private	Optional<TerminalFunction> terminalFunction = Optional.empty();
		private Set<String> dependencies = new HashSet<>();
		private StringBuilder text = new StringBuilder();
		private	Optional<String> nameLiteral = Optional.empty();
		
		public Expression(boolean isSubExpression) {
			this.isSubExpression = isSubExpression;
		}

		public Table evaluate(Pinto pinto) {
			if(!terminalFunction.isPresent()) {
				throw new PintoSyntaxException("Need terminal to evaluate expression.");
			}
			return terminalFunction.get().apply(pinto, this);
		}

		@Override
		public void accept(Pinto pinto, Table table) {
			try {
				this.indexer.orElse(new Indexer(true)).accept(pinto, table);
				for(int i = 0; i < functions.size(); i++) {
					functions.get(i).accept(pinto, table);
				}
				table.collapseFunction();
			} catch(RuntimeException e) {
				throw new PintoSyntaxException("Error in expression: \"" + getText().trim() + "\": " + e.getLocalizedMessage() + "\n",e);
			}
		}
		
		public void addIndexer(Indexer indexer) {
			if(this.indexer == null) {
				indexer.setIndexForExpression();
				this.indexer = Optional.of(indexer);
			} else {
				functions.add(indexer);
			}
		}

		public void addFunction(Column<?> col) {
			addFunction((StackFunction) (p, s) -> s.addFirst(col));
		}

		public void addFunction(TableFunction function) {
			if(this.indexer == null) {
				this.indexer = Optional.empty();
			}
			functions.add(function);
		}

		public void setTerminal(TerminalFunction function) {
			if(isSubExpression) {
				throw new PintoSyntaxException("Sub-expressions cannot include terminal functions");
			}
			this.terminalFunction = Optional.of(function);
		}

		public ArrayList<TableFunction> getFunctions() {
			return functions;
		}
		
		public boolean isExpressionStart() {
			return functions.size() == 0;
		}

		public boolean isNullary() {
			return (!indexer.isPresent()) || indexer.get().isNone();
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
	
	public static abstract class DependentColumnStackFunction<T> implements StackFunction {

		protected final Cache<String, List<T>> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofSeconds(30))
			.build(); 
		protected final Class<T> type;
		protected final List<String> headers = new ArrayList<>();
		
		protected DependentColumnStackFunction(Class<T> type) {
			this.type = type;
		}

		abstract protected void setup(Stack stack);
		abstract protected Callable<List<T>> run(PeriodicRange<?> range);
		
		public void accept(Pinto pinto, Stack stack) {
			setup(stack);
			for(int i = 0; i < headers.size(); i++) {
				stack.addLast(getColumn(i));
			}
		}
		
		private Column<T> getColumn(int col) {
			return new Column<T>(type, i -> headers.get(col) , (range, i) -> {
				try {
					return cache.get(range.getId(), run(range)).get(col);
				} catch (Throwable e) { throw new RuntimeException(e); }
			});
		}
	}
	
	
}
