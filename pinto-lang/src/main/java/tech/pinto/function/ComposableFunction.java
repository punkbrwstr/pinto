package tech.pinto.function;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;

public abstract class ComposableFunction implements Cloneable {
    

	protected final Optional<String> name;
    protected Optional<ComposableFunction> previousFunction;
    protected final Indexer indexer;
	protected final ParameterType parameterType;
	private Optional<Supplier<String[]>> argumentSupplier = Optional.empty();
	private String[] cachedArguments = null; 
    protected boolean subFunction = false;
    

    public ComposableFunction(String name, ComposableFunction previousFunction, Indexer indexer) {
    	this(Optional.of(name),Optional.of(previousFunction), indexer,ParameterType.arguments_optional);
    }

    public ComposableFunction(String name,ComposableFunction previousFunction, Indexer indexer, ParameterType parameterType) {
    	this(Optional.of(name),Optional.of(previousFunction), indexer,parameterType );
    }

    protected ComposableFunction(Optional<String> name, Optional<ComposableFunction> previousFunction,
    		Indexer indexer, ParameterType parameterType) {
    	this.parameterType = parameterType;
        this.name = name;
        this.indexer = indexer;
        this.previousFunction = previousFunction;
    }
    
	abstract protected LinkedList<Column> apply(LinkedList<Column> stack);
	
    public LinkedList<Column> compose() throws PintoSyntaxException {
    	LinkedList<Column> inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
    	LinkedList<Column> outputs = new LinkedList<>();
    	for(LinkedList<Column> indexedInputs : indexer.index(inputs)) {
    		if(!argumentSupplier.isPresent()) {
    			parseArgs(indexedInputs);
    		}
    		outputs.addAll(apply(indexedInputs));
    	} 
    	outputs.addAll(inputs);
    	return outputs;
    }
    
    private void parseArgs(LinkedList<Column> inputs) throws PintoSyntaxException {
    	if(parameterType.equals(ParameterType.arguments_optional) || 
    			parameterType.equals(ParameterType.arguments_required)) {
    		if(inputs.size() > 0 && inputs.getFirst().getHeaderFunction().isPresent() &&
    				! inputs.getFirst().getSeriesFunction().isPresent()) {
    			final Column argumentInput = inputs.removeFirst();
    			argumentSupplier = Optional.of(() -> argumentInput.toString().split(","));
    		} else {
    			if(parameterType.equals(ParameterType.arguments_required)) {
    				throw new PintoSyntaxException("Function \"" + name + "\" requires arguments.");
    			}
    		}
    	}
    }

	protected String[] getArgs() {
		if(cachedArguments == null) {
			cachedArguments = argumentSupplier.orElse(() -> new String[]{}).get();
		}
		return cachedArguments;
	}
	
    public Optional<ComposableFunction> getPrevious() {
		return previousFunction;
	}

	final public String toString() {
    	String output = name.orElse("HEAD");
    	if(getArgs().length > 0) {
    		output += Stream.of(getArgs()).collect(Collectors.joining(",", "(", ")"));
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
			//clone.indexer = indexer.clone();
			clone.cachedArguments = null;
			return clone;
		} catch (CloneNotSupportedException e) { throw new RuntimeException(); }
	}
    
    public boolean isHead() {
    	return !previousFunction.isPresent();
    }

	public Indexer getIndexer() {
		return indexer;
	}

	public Optional<String> getName() {
		return name;
	}

	public ParameterType getParameterType() {
		return parameterType;
	}

    protected static String join(String... s) {
        return Stream.of(s).collect(Collectors.joining(" "));
    }

    protected static LinkedList<Column> asList(Column... functions) {
    	return Arrays.stream(functions).collect(Collectors.toCollection(() -> new LinkedList<Column>()));
    }
    
}
