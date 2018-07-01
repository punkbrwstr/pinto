package tech.pinto;

import java.util.function.Consumer;
import java.util.function.Function;

import tech.pinto.Pinto.StackFunction;
import tech.pinto.Pinto.TableFunction;

public class Name implements Function<Pinto,Consumer<Table>> {

	final private String name;
	final private Pinto.TableFunction function;
	final private Indexer indexer;
	final private boolean isTerminal;
	final private boolean startFromLast;
	final private boolean isBuiltIn;
	final private String description;

	public Name(String name, Pinto.TableFunction function, Indexer indexer,
			boolean isTerminal, boolean startFromLast, boolean isBuiltIn, String description) {
		this.name = name;
		this.function = function;
		this.indexer = indexer;
		this.isTerminal = isTerminal;
		this.startFromLast = startFromLast;
		this.description = description;
		this.isBuiltIn = isBuiltIn;
	}


	public Consumer<Table> getFunction(Pinto pinto) {
		return t -> function.accept(pinto, t);
	}
	
	public Consumer<Table> getDefaultIndexer(Pinto pinto) {
		return t -> {
			try {
				indexer.apply(pinto).accept(t);
			} catch(Throwable e) {
				throw new PintoSyntaxException("Indexer error for \"" + name + "\": " + e.getLocalizedMessage() , e);
			}
		};
	}
	
	public boolean terminal() {
		return isTerminal;
	}
	
	public boolean startFromLast() {
		return startFromLast;
	}

	public boolean builtIn() {
		return isBuiltIn;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getIndexString() {
		return indexer.toString();
	}
	
	public String getHelp(String name) {
		StringBuilder sb = new StringBuilder();
    	String crlf = System.getProperty("line.separator");
		sb.append(name).append(" help").append(crlf);
		sb.append("\t").append("Default indexer: ").append(getIndexString()).append(crlf);
		sb.append("\t").append(description).append(crlf);
		return sb.toString();
	}

	@Override
	public Consumer<Table> apply(Pinto arg0) {
		return t -> {
			try {
				function.accept(arg0, t);
			} catch(Throwable e) {
				throw new PintoSyntaxException("Error in \"" + name + "\"" , e);
			}
		};
	}

	@Override
	public String toString() {
        return name;
    }
	
	public static Builder nameBuilder(String name, StackFunction function) {
		return new Builder(name, function.toTableFunction());
	}

	public static Builder nameBuilder(String name, TableFunction function) {
		return new Builder(name, function);
	}
	
	public static class Builder {
		final private String name;
		final private Pinto.TableFunction function;
		private Indexer indexer;
		private boolean isTerminal;
		private boolean startFromLast;
		private boolean isBuiltIn;
		private String description;

		public Builder(String name, TableFunction function) {
			this.name = name;
			this.function = function;
			this.indexer = Indexer.ALL;
			this.isBuiltIn = true;
			this.isTerminal = false;
			this.startFromLast = false;
		}
		
		public Builder indexer(Indexer indexer) {
			this.indexer = indexer;
			return this;
		}

		public Builder indexer(String index) {
			this.indexer = new Indexer(index.replaceAll("^\\[|\\]$", ""), false);
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder terminal() {
			this.isTerminal = true;
			return this;
		}

		public Builder defined() {
			this.isBuiltIn = false;
			return this;
		}

		public Builder startFromLast() {
			this.startFromLast = true;
			return this;
		}
		
		
		public Name build() {
			return new Name(name, function, indexer, isTerminal, startFromLast, isBuiltIn, description);
		}
		
		
		
	}

}
