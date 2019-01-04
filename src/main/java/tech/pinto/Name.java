package tech.pinto;

import java.util.HashSet;
import java.util.Optional;

import tech.pinto.Pinto.StackFunction;
import tech.pinto.Pinto.TableFunction;
import tech.pinto.Pinto.TerminalFunction;

public class Name {

	final private String name;
	final private Optional<TableFunction> tableFunction;
	final private Optional<TerminalFunction> terminalFunction;
	final private Optional<String> indexString;
	final private boolean isBuiltIn;
	final private String description;
	private Indexer indexer = null;
	
	public Name(String name, Optional<TableFunction> tableFunction, Optional<TerminalFunction> terminalFunction,
			Optional<String> indexString, boolean isBuiltIn, String description) {
		this.name = name;
		this.tableFunction = tableFunction;
		this.terminalFunction = terminalFunction;
		this.indexString = indexString;
		this.isBuiltIn = isBuiltIn;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public Indexer getIndexer(Pinto pinto) {
		if(indexer == null) {
			indexer = indexString.isPresent() ? new Indexer(pinto, new HashSet<>(), indexString.get()) :
						new Indexer(pinto, new HashSet<>(), ":");
		}
		return indexer;
	}


	public TableFunction getTableFunction() {
		return tableFunction.get();
	}

	public TerminalFunction getTerminalFunction() {
		return terminalFunction.get();
	}

	public boolean isTerminal() {
		return terminalFunction.isPresent();
	}

	public boolean isBuiltIn() {
		return isBuiltIn;
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
		return new Builder(name, function);
	}

	public static Builder nameBuilder(String name, TableFunction function) {
		return new Builder(name, function);
	}

	public static Builder terminalNameBuilder(String name, TerminalFunction function) {
		return new Builder(name, function);
	}
	
	public static class Builder {
		private final String name;
		private final Optional<TableFunction> tableFunction;
		private final Optional<TerminalFunction> terminalFunction;
		private Optional<String> indexer = Optional.empty();
		private boolean isBuiltIn = true;
		private String description = "";

		public Builder(String name, TableFunction tableFunction) {
			this.name = name;
			this.tableFunction = Optional.of(tableFunction);
			this.terminalFunction = Optional.empty();
		}

		public Builder(String name, TerminalFunction terminalFunction) {
			this.name = name;
			this.terminalFunction = Optional.of(terminalFunction);
			this.tableFunction = Optional.empty();
		}
		
		public Builder indexer(String index) {
			this.indexer = Optional.of(index);
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder defined() {
			this.isBuiltIn = false;
			return this;
		}

		public Name build() {
			return new Name(name, tableFunction, terminalFunction, indexer, isBuiltIn, description);
		}
		
		
		
	}

}
