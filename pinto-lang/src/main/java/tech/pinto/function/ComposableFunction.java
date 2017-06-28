package tech.pinto.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;

public class ComposableFunction implements Cloneable {
    

	protected final Optional<String> name;
    protected String[] args;
    protected Indexer indexer;
    protected Optional<ComposableFunction> previousFunction;
    protected ComposableFunction nextFuction;
    protected boolean subFunction = false;
    

    public ComposableFunction(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
    	this(Optional.of(name),Optional.of(previousFunction), indexer, args);
    }

    protected ComposableFunction(Optional<String> name, Optional<ComposableFunction> previousFunction, Indexer indexer, String... args) {
        this.name = name;
        this.args = args;
        this.indexer = indexer;
        this.previousFunction = previousFunction;
        this.previousFunction.ifPresent(f -> f.setNext(this));
    }
    
	public LinkedList<Column> composeIndexed(LinkedList<Column> stack) {
		return stack;
	}

    public LinkedList<Column> compose() throws PintoSyntaxException {
    	LinkedList<Column> inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
    	LinkedList<Column> outputs = new LinkedList<>();
    	int i = 0;
    	do {
    		try {
    			outputs.addAll(composeIndexed(indexer.index(inputs)));
    		} catch(PintoSyntaxException pse) {
    			if(i > 0) {
    				break;
    			} else {
    				throw pse;
    			}
    		}
    		i++;
    	} while(indexer.isRepeated() && inputs.size() > 0);
    	outputs.addAll(inputs);
    	return outputs;
    }
    
	public void setNext(ComposableFunction nextFunction) {
		this.nextFuction = nextFunction;
	}

	public void setPrevious(ComposableFunction previousFunction) {
		this.previousFunction = Optional.of(previousFunction);
        this.previousFunction.ifPresent(f -> f.setNext(this));
	}
	
    public Optional<ComposableFunction> getPrevious() {
		return previousFunction;
	}

	final public String toString() {
    	String output = name.orElse("HEAD");
    	if(args.length > 0) {
    		output += Stream.of(args).collect(Collectors.joining(",", "(", ")"));
    	}
        return output;	
    }

    public Set<String> getDependencies() {
    	Set<String> dependencies = previousFunction.isPresent() ?
    			previousFunction.get().getDependencies() : new HashSet<>();
    	name.ifPresent(dependencies::add);
    	return dependencies;
    }

    public StringBuilder toExpression() {
    	StringBuilder expression = previousFunction.isPresent() ?
    			previousFunction.get().toExpression() : new StringBuilder();
    	if(name.isPresent() && ! subFunction) {
    		if(!indexer.isEverything()) {
    			expression.append(indexer.toString()).append(" ");
    		}
    		expression.append(toString()).append(" ");
    	}
    	return expression;
    }
    
    public StringBuilder toExpressionTrace() {
    	StringBuilder expression = previousFunction.isPresent() ?
    			previousFunction.get().toExpressionTrace() : new StringBuilder();
    	expression.append(indexer.toString()).append(toString()).append("->");
    	return expression;
    }
    
    public void setIsSubFunction() {
    	subFunction = true;
    	previousFunction.ifPresent(f -> f.setIsSubFunction());

    }

    public ComposableFunction getHead() {
    	return previousFunction.isPresent() ? previousFunction.get().getHead() : this;
    }

    public Optional<ComposableFunction> getSecondToHead() {
    	if(!previousFunction.isPresent()) {
    		return Optional.empty();
    	}
    	return previousFunction.get().previousFunction.isPresent() ? previousFunction.get().getSecondToHead() : Optional.of(this);
    }

    @Override
	protected Object clone() {
		ComposableFunction clone;
		try {
			clone = (ComposableFunction) super.clone();
			clone.previousFunction = previousFunction.isPresent() ? 
					Optional.of((ComposableFunction) previousFunction.get().clone()) : Optional.empty();
			clone.indexer = indexer.clone();
			return clone;
		} catch (CloneNotSupportedException e) { throw new RuntimeException(); }
	}
    
    protected static <T> LinkedList<T> reverse(LinkedList<T> list) {
		LinkedList<T> reversed = new LinkedList<>();
		list.stream().forEach(reversed::addFirst);
		return reversed;
    }
	
    protected static String join(String... s) {
        return Stream.of(s).collect(Collectors.joining(" "));
    }

    protected static String join(Stream<String> s1, String... s) {
        return Stream.concat(s1, Stream.of(s)).collect(Collectors.joining(" "));
    }
    
    protected static String join(Collection<String> s) {
    	return s.stream().collect(Collectors.joining(" "));
    }

    protected static LinkedList<Column> asList(Column... functions) {
    	return Arrays.stream(functions).collect(Collectors.toCollection(() -> new LinkedList<Column>()));
    }
    
    public boolean isHead() {
    	return !previousFunction.isPresent();
    }

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public Indexer getIndexer() {
		return indexer;
	}

	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	public void setPreviousFunction(Optional<ComposableFunction> previousFunction) {
		this.previousFunction = previousFunction;
	}

	public Optional<String> getName() {
		return name;
	}

}
