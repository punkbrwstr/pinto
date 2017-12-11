package tech.pinto;

import java.util.Optional;

import java.util.function.Consumer;
import java.util.function.Function;

public class Name implements Function<Pinto,Consumer<Table>> {

	final private String name;
	final private boolean isBuiltIn;
	final private Function<Pinto,Consumer<Table>> function;
	private Optional<Indexer> indexer;
	final private Optional<String> indexString;
	final private boolean isTerminal;
	final private boolean startFromLast;
	final private String description;


	public Name(String name, Consumer<Table> function, String defaultIndexString, String description) {
		this(name, p -> function,  Optional.empty(), Optional.of(defaultIndexString), description, false, false, true);
	}

	public Name(String name, Function<Pinto,Consumer<Table>> function, String indexString,
			String description, boolean isTerminal) {
		this(name, function,  Optional.empty(), Optional.of(indexString), description, isTerminal, false, true);
	}

	public Name(String name, Function<Pinto,Consumer<Table>> function, Optional<Indexer> indexer, Optional<String> indexString,
			String description, boolean isTerminal, boolean startFromLast, boolean isBuiltIn) {
		this.name = name;
		this.function = function;
		this.indexer = indexer;
		this.indexString = indexString;
		this.isTerminal = isTerminal;
		this.startFromLast = startFromLast;
		this.description = description;
		this.isBuiltIn = isBuiltIn;
	}


	public Consumer<Table> getFunction(Pinto pinto) {
		return function.apply(pinto);
	}
	
	public boolean hasDefaultIndexer() {
		return indexer.isPresent() || indexString.isPresent();
	}
	
	public Consumer<Table> getDefaultIndexer(Pinto pinto) {
		if(!indexer.isPresent()) {
			indexer = Optional.of(new Indexer(pinto, indexString.get(), false));
		}
		return t -> {
			try {
				indexer.get().accept(t);
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
		return indexer.isPresent() ? indexer.get().toString() : indexString.get();
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
				function.apply(arg0).accept(t);
			} catch(Throwable e) {
				throw new PintoSyntaxException("Error in \"" + name + "\"" , e);
			}
		};
	}

	@Override
	public String toString() {
        return name;
    }

}
