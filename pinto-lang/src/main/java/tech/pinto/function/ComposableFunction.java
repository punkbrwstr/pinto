package tech.pinto.function;

import java.util.Arrays;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;

public class ComposableFunction implements Cloneable {
    

	protected final String name;
    protected final Indexer indexer;
    protected Optional<Parameters> parameters = Optional.empty();
    protected Optional<ComposableFunction> previousFunction;
    protected Optional<LinkedList<Column>> inputs = Optional.empty();
    protected boolean subFunction = false;
    

    public ComposableFunction(Indexer indexer) {
    	this("HEAD", Optional.empty(), indexer);
    }

    public ComposableFunction(String name, ComposableFunction previousFunction, Indexer indexer) {
    	this(name, Optional.of(previousFunction), indexer);
    }

    protected ComposableFunction(String name, Optional<ComposableFunction> previousFunction,
    		Indexer indexer) {
        this.name = name;
        this.indexer = indexer;
        this.previousFunction = previousFunction;
    }
    
	protected void apply(LinkedList<Column> stack) {
		
	}

    public LinkedList<Column> compose() {
    	LinkedList<Column> inputStack = previousFunction.map(ComposableFunction::compose).orElse(
    			inputs.orElse(new LinkedList<>()));
    	LinkedList<Column> outputStack = new LinkedList<>();
    	List<LinkedList<Column>> indexedStacks = inputs.isPresent() ? Indexer.ALL.index(inputStack) : indexer.index(inputStack);
    	if(!indexedStacks.isEmpty() && parameters.isPresent()) {
    		parameters.get().parseArguments(indexedStacks.get(0));
    	}
    	for(LinkedList<Column> indexedStack : indexedStacks) {
   			apply(indexedStack);
    		outputStack.addAll(indexedStack);
    	} 
    	outputStack.addAll(inputStack);
    	return outputStack;
    }
    
    public Optional<ComposableFunction> getPrevious() {
		return previousFunction;
	}

	final public String toString() {
		return parameters.isPresent() ? join(parameters.get().toString(),name) : name;
    }

    public Set<String> getDependencies() {
    	Set<String> dependencies = previousFunction.isPresent() ?
    			previousFunction.get().getDependencies() : new HashSet<>();
    	dependencies.add(name);
    	return dependencies;
    }

    public StringBuilder toExpression() {
    	StringBuilder expression = previousFunction.isPresent() ?
    			previousFunction.get().toExpression() : new StringBuilder();
    	if(! subFunction) {
    		expression.append(indexer.toString()).append(" ");
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
    
	public void setInputs(LinkedList<Column> inputs) {
		this.inputs = Optional.of(inputs);
	}

    public ComposableFunction getHead() {
    	return previousFunction.isPresent() ? previousFunction.get().getHead() : this;
    }

    public boolean isHead() {
    	return !previousFunction.isPresent();
    }
    
    public Indexer getIndexer() {
    	return indexer;
    }

    @Override
	protected Object clone() {
		ComposableFunction clone;
		try {
			clone = (ComposableFunction) super.clone();
			clone.previousFunction = previousFunction.isPresent() ? 
					Optional.of((ComposableFunction) previousFunction.get().clone()) : Optional.empty();
			//clone.indexer = indexer.clone();
			return clone;
		} catch (CloneNotSupportedException e) { throw new RuntimeException(); }
	}

    protected static String join(String... s) {
        return Stream.of(s).collect(Collectors.joining(" "));
    }

    protected static LinkedList<Column> asList(Column... functions) {
    	return Arrays.stream(functions).collect(Collectors.toCollection(() -> new LinkedList<Column>()));
    }
    

    
    
}
