package tech.pinto.function;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class Function implements Cloneable {
    
    protected final String name;
    protected final String[] args;
    protected LinkedList<Function> inputStack;
    protected int outputCount, evaluationCount = 0;
    

    public Function(String name, LinkedList<Function> inputStack, String... args) {
        this.name = name;
        this.outputCount = 0;
        this.inputStack = inputStack;
        this.args = args;
    }

    abstract public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range);

    abstract public Function getReference();
    
    public Set<String> getDependencies() {
        return inputStack.stream().flatMap(d -> d.getDependencies().stream()).collect(Collectors.toSet()); 
    }
    
	public int getOutputCount() {
		return outputCount;
	}

	public String getName() {
		return name;
	}
    
    public String toString() {
    	String output = getName();
    	if(args.length > 0) {
    		output += Stream.of(args).collect(Collectors.joining(",", "(", ")"));
    	}
        return output;
    }

	public Function clone() {
		try {
			Function clone = (Function) super.clone();
			clone.inputStack = new LinkedList<>();
			inputStack.stream().map(Function::clone).forEach(clone.inputStack::addLast);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
    }
	
    protected String joinWithSpaces(String... s) {
        return Stream.of(s).collect(Collectors.joining(" "));
    }

    protected String joinWithSpaces(Stream<String> s1, String... s) {
        return Stream.concat(s1, Stream.of(s)).collect(Collectors.joining(" "));
    }
    
    protected String joinWithSpaces(Collection<String> s) {
    	return s.stream().collect(Collectors.joining(" "));
    }


}
