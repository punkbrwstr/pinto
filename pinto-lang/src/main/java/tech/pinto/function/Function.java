package tech.pinto.function;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class Function implements Cloneable {
    
    protected Optional<String> name;
    protected java.util.function.Function<Function,String> labeller;
    protected final String[] args;
    protected LinkedList<Function> inputStack;
    protected int evaluationCount = 0;
	public String cloneOrNot = "non-clone";
    

    public Function(String name, LinkedList<Function> inputStack, String... args) {
    	this(Optional.of(name), inputStack, args);
    }

    public Function(Optional<String> name, LinkedList<Function> inputStack, String... args) {
        this.name = name;
        this.inputStack = inputStack;
        this.args = args;
        labeller = f -> {
        	String output = f.name.orElse("anonymous");
        	if(f.args.length > 0) {
        		output += Stream.of(args).collect(Collectors.joining(",", "(", ")"));
        	}
            return output;	
        };
    }

    abstract public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range);

    abstract public Function getReference();
    
	abstract public int getOutputCount();
	
	public LinkedList<Function> getStack() {
		return inputStack;
	}

	public void setLabeller(java.util.function.Function<Function,String> labeller) {
		this.labeller = labeller;
	}
	
    final public String toString() {
    	//System.out.println("Calling toString on a " + cloneOrNot + ". And I have " + inputStack.size() + " inputs.");
    	return labeller.apply(this);
    }

    public Set<String> getDependencies() {
        return inputStack.stream().flatMap(f -> f.inputStack.stream())
        		.filter(f -> f.name.isPresent()).map(f -> f.name.get()).collect(Collectors.toSet()); 
    }
    
    public void getPlaceholders(LinkedList<PlaceholderFunction> placeholders) {
    	for(Function f : inputStack) {
    		f.getPlaceholders(placeholders);
    	}
    }
    
    public void getLeafNodes(LinkedList<Function> leaves) {
   		if(inputStack.size() == 0) {
   			leaves.addFirst(this);
   		}
    	for(Function f : inputStack) {
    		f.getLeafNodes(leaves);
    	}
    }

	public Function clone() {
		try {
			Function clone = (Function) super.clone();
			clone.inputStack = new LinkedList<>();
			inputStack.stream().map(Function::clone).forEach(clone.inputStack::addLast);
			clone.cloneOrNot = "clone";
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
    }
	
    protected String join(String... s) {
        return Stream.of(s).collect(Collectors.joining(" "));
    }

    protected String join(Stream<String> s1, String... s) {
        return Stream.concat(s1, Stream.of(s)).collect(Collectors.joining(" "));
    }
    
    protected String join(Collection<String> s) {
    	return s.stream().collect(Collectors.joining(" "));
    }


}
