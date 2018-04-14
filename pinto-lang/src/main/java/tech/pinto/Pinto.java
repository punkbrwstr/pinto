package tech.pinto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class Pinto {
	
	static Pattern STRING_LITERAL = Pattern.compile("\"(.*?)\"");
	static Pattern INDEXER = Pattern.compile("\\[(.*?)\\]");
	static Pattern HEADER_LITERAL = Pattern.compile("\\{(.*)\\}");
	static Pattern NAME_LITERAL = Pattern.compile(":(\\S+)");
	static Pattern ILLEGAL_NAME = Pattern.compile(".*[\\{\\}\\[\\]\"\\s:].*");
	private static int port;

	@Inject
	Namespace namespace;
	State engineState = new State(false);

	@Inject
	public Pinto() {
	}

	public List<Table> eval(String expression) {
		return evaluate(expression, engineState);
	}

	public LinkedList<Column<?,?>> parseSubExpression(String expression) {
		Table t = evaluate(expression, new State(true)).get(0);
		return t.peekStack();
	}

	public List<Table> evaluate(String expression, State state) {
		List<Table> responses = new ArrayList<>();
		try (Scanner sc = new Scanner(expression)) {
			while (sc.hasNext()) {
				if(state.isExpressionStart()) {
					if(sc.hasNext(NAME_LITERAL)) { // name literal
						String nl = sc.next(NAME_LITERAL).replaceAll(":", "");
						state.setNameLiteral(checkName(nl));
						String functionIndexString = sc.hasNext(INDEXER) ? sc.next(INDEXER) : "[:]";
						state.setNameIndexer(new Indexer(this, functionIndexString, true));
						state.setExpression(expression);
					}
				}
				if (sc.hasNextDouble()) { // double literal
					double d = sc.nextDouble();
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantDoubles(d));
					})));
				} else if (sc.hasNext(Pattern.compile("\".*?"))) { // string literal
					final String sl = parseBlock(sc,"\"").replaceAll("\"", "");
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(s -> {
						s.addFirst(new tech.pinto.Column.OfConstantStrings(sl,sl));
					})));
				} else if (sc.hasNext(Pattern.compile("\\[.*?"))) { // indexer
					state.setCurrent(state.getCurrent().andThen(
							new Indexer(this, parseBlock(sc,"]"), false)));
				} else if (sc.hasNext(Pattern.compile("\\{.*?"))) { // header literal
					state.setCurrent(state.getCurrent().andThen(toTableConsumer(
							new HeaderLiteral(this, parseBlock(sc, "}")))));
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
		return toTableConsumer(colsFunction, false);
	}

	public static Consumer<Table> toTableConsumer(Consumer<LinkedList<Column<?,?>>> colsFunction, boolean clearBase) {
		return t -> {
			LinkedList<Column<?,?>> endingStack = new LinkedList<>();
			for(LinkedList<Column<?,?>> cols : t.popStacks()) {
				colsFunction.accept(cols);
				endingStack.addAll(0,cols);
			}
			t.pushToBase(endingStack, clearBase);
		};
	}

	private static String checkName(String name) {
		if(ILLEGAL_NAME.matcher(name).matches()) {
			throw new PintoSyntaxException("Illegal character in name literal \"" + name + "\"");
		}
		return name;
	}
	
	
	private String parseBlock(Scanner scanner, String closing) throws PintoSyntaxException {
		StringBuilder sb = new StringBuilder();
		String next = null;
		do {
			if (!scanner.hasNext()) {
				throw new PintoSyntaxException("Missing " + closing);
			}
			next = next == null ? scanner.next().replaceAll("^" + closing, "") : " " + scanner.next();
			sb.append(next);
		} while (!next.contains(closing));
		return sb.toString();
	}
	
	public static class State {
		
		private final boolean isSubExpression;
		private boolean expressionStart = true;
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
			dependencies = new ArrayList<>();
			expression = Optional.empty();
			nameLiteral = Optional.empty();
			nameIndexer = Optional.empty();
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
	
	public static void main(String[] s) {
		LinkedList<String> l = new LinkedList<>();
		l.addFirst("one");
		l.addFirst("two");
		l.size();
	}
}
