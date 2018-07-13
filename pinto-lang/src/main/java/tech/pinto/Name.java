package tech.pinto;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import tech.pinto.Pinto.StackFunction;
import tech.pinto.Pinto.TableFunction;

public class Name {

	final private String name;
	final private Pinto.TableFunction function;
	final private Optional<Indexer> indexer;
	final private boolean isTerminal;
	final private boolean isBuiltIn;
	final private boolean isSkipEvaluation;
	final private String description;

	public Name(String name, Pinto.TableFunction function, Optional<Indexer> indexer,
			boolean isTerminal, boolean isBuiltIn, boolean isSkipEvaluation, String description) {
		this.name = name;
		this.function = function;
		this.indexer = indexer;
		this.isTerminal = isTerminal;
		this.description = description;
		this.isBuiltIn = isBuiltIn;
		this.isSkipEvaluation = isSkipEvaluation;
	}


	public Consumer<Table> getConsumer(Pinto pinto, Set<String> dependencies) {
		Consumer<Table> c = t -> {};
		if(isTerminal && !isSkipEvaluation) {
			c = pinto.getExpression();
		}
		if(indexer.isPresent()) {
			c =  c.andThen(indexer.get().getConsumer(pinto, dependencies, false));
		} 
		return c.andThen(t -> function.accept(pinto, t));
	}
	
	public boolean terminal() {
		return isTerminal;
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
		if(indexer.isPresent()) {
			sb.append("\t").append("Default indexer: ").append(getIndexString()).append(crlf);
		}
		sb.append("\t").append(description).append(crlf);
		return sb.toString();
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
		private Optional<Indexer> indexer = Optional.empty();
		private boolean isTerminal;
		private boolean isBuiltIn;
		private boolean isSkipEvaluation;
		private String description;

		public Builder(String name, TableFunction function) {
			this.name = name;
			this.function = function;
			this.isBuiltIn = true;
			this.isTerminal = false;
			this.isSkipEvaluation = false;
		}
		
		public Builder indexer(Indexer indexer) {
			this.indexer = Optional.of(indexer);
			return this;
		}

		public Builder indexer(String index) {
			this.indexer = Optional.of(new Indexer(index.replaceAll("^\\[|\\]$", "")));
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

		public Builder skipEvaluation() {
			this.isSkipEvaluation = true;
			return this;
		}

		public Name build() {
			return new Name(name, function, indexer, isTerminal, isBuiltIn, isSkipEvaluation, description);
		}
		
		
		
	}

}
