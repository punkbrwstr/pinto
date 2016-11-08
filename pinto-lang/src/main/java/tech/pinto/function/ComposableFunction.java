package tech.pinto.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		return stack;
	}

    public LinkedList<EvaluableFunction> compose() throws PintoSyntaxException {
    	LinkedList<EvaluableFunction> inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
    	LinkedList<EvaluableFunction> outputs = composeIndexed(indexer.index(inputs));
    	if(indexer.isReverse()) {
    		outputs = reverse(outputs);
    	}
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
    		if(indexer.isReverse() || !indexer.isEverything()) {
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

    protected static LinkedList<EvaluableFunction> asList(EvaluableFunction... functions) {
    	return Arrays.stream(functions).collect(Collectors.toCollection(() -> new LinkedList<EvaluableFunction>()));
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
