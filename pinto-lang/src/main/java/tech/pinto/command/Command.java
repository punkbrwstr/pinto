package tech.pinto.command;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.PintoSyntaxException;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class Command<IT,ID extends Data<IT>,OT, OD extends Data<OT>> implements Cloneable {
    
    protected final String name;
    protected final Class<ID> inputType;
    protected final Class<OD> outputType;
    protected ArrayDeque<Command<?,?,IT,ID>> inputStack = new ArrayDeque<>();
    protected ArrayDeque<OD> outputStack = null;
    protected int inputCount, outputCount;
    

    public Command(String name, Class<ID> inputType, Class<OD> outputType) {
        this.name = name;
        this.inputType = inputType;
        this.outputType = outputType;
        this.inputCount = Integer.MAX_VALUE;
        this.outputCount = Integer.MAX_VALUE;
    }

    abstract protected <P extends Period> ArrayDeque<OD> evaluate(PeriodicRange<P> range);
    
    
    public ArrayDeque<OD> getOutputData(PeriodicRange<?> range) {
        if(outputStack == null) {
            outputStack = evaluate(range);
        }
        return outputStack;
    }

    public Set<String> getDependencies() {
        return inputStack.stream().flatMap(d -> d.getDependencies().stream()).collect(Collectors.toSet()); 
    }
    
    public boolean isTerminal() {
        return false;
    }
    
    public String toString() {
        return name;
    }

    public String summarize() {
        return joinWithSpaces(inputStack.stream().map(Command::toString), toString());
    }

    @SuppressWarnings("unchecked")
	public Command<IT, ID, OT, OD> clone() {
		try {
			Command<IT, ID, OT, OD> clone = (Command<IT, ID, OT, OD>) super.clone();
			clone.inputStack = new ArrayDeque<>();
			clone.outputStack = null;
			inputStack.stream().map(Command::clone).forEach(clone.inputStack::addLast);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
    }
    
    public ArrayDeque<Command<?,?,IT,ID>> getInputCommands() {
        return inputStack;
    }

    public Class<ID> getInputType() {
        return inputType;
    }

    public int inputCount() {
        return inputCount;
    }

    public Class<OD> getOutputType() {
        return outputType;
    }

	protected void determineOutputCount() {

	}

	public int outputCount() {
		if(outputCount == Integer.MAX_VALUE) {
			determineOutputCount();
		}
		return outputCount;
	}

	public void decrementOutputCountBy(int i) {
		if(outputCount == Integer.MAX_VALUE) {
			determineOutputCount();
		}
        outputCount -= i;
	}
    
    protected ArrayDeque<ID> getInputData(PeriodicRange<?> range) {
    	ArrayDeque<ID> inputs = new ArrayDeque<>();
    	ArrayDeque<ArrayDeque<ID>> allInputs = inputStack.stream().map(c -> c.getOutputData(range))
    			.collect(Collectors.toCollection(() -> new ArrayDeque<>()));
    	int inputsNeeded = inputCount;
    	while((inputCount == Integer.MAX_VALUE || inputsNeeded > 0) && !allInputs.isEmpty()) {
    		ArrayDeque<ID> d = allInputs.remove();
    		while((inputCount == Integer.MAX_VALUE || inputsNeeded > 0) && !d.isEmpty()) {
    			inputs.addLast(d.removeFirst());
    			inputsNeeded--;
    		}
    	}
    	if(inputCount != Integer.MAX_VALUE && inputsNeeded > 0) {
    		throw new IllegalArgumentException("Not enough arguments for " + name);
    	}
        return inputs;
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
