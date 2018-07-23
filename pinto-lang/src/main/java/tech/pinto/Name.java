package tech.pinto;

import java.util.HashSet;
import java.util.Optional;

import tech.pinto.Pinto.StackFunction;
import tech.pinto.Pinto.TableFunction;

public class Name {

	final private String name;
	final private Pinto.TableFunction function;
	final private Optional<String> indexString;
	final private boolean isTerminal;
	final private boolean isBuiltIn;
	final private boolean isSkipEvaluation;
	final private String description;
	private Indexer indexer = null;

	public Name(String name, Pinto.TableFunction function, Optional<String> indexer,
			boolean isTerminal, boolean isBuiltIn, boolean isSkipEvaluation, String description) {
		this.name = name;
		this.function = function;
		this.indexString = indexer;
		this.isTerminal = isTerminal;
		this.description = description;
		this.isBuiltIn = isBuiltIn;
		this.isSkipEvaluation = isSkipEvaluation;
	}
	
	public String getName() {
		return name;
	}

	public Pinto.TableFunction getFunction() {
		return function;
	}

	public Indexer getIndexer(Pinto pinto) {
		if(indexer == null) {
			indexer = indexString.isPresent() ? new Indexer(pinto, new HashSet<>(), indexString.get()) :
						new Indexer(pinto, new HashSet<>(), ":");
		}
		return indexer;
	}

	public boolean isTerminal() {
		return isTerminal;
	}

	public boolean isBuiltIn() {
		return isBuiltIn;
	}

	public boolean isSkipEvaluation() {
		return isSkipEvaluation;
	}

	public String getDescription() {
		return description;
	}
	
	public String getIndexString() {
		return indexString.orElse("[:]");
	}
	
	public String getHelp(String name) {
		StringBuilder sb = new StringBuilder();
    	String crlf = System.getProperty("line.separator");
		sb.append(name).append(" help").append(crlf);
		if(indexString.isPresent()) {
			sb.append("\t").append("Default indexer: ").append(indexString).append(crlf);
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
		private Optional<String> indexer = Optional.empty();
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
		
		public Builder indexer(String index) {
			this.indexer = Optional.of(index);
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
