package tech.pinto.command;

import java.util.ArrayDeque;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class Command implements Cloneable {
    
    protected final String name;
    protected final Class<? extends Data<?>> inputType;
    protected final Class<? extends Data<?>> outputType;
    protected ArrayDeque<Command> inputStack = new ArrayDeque<>();
    protected int inputCount, outputCount, evaluationCount = 0;
    

    public Command(String name, Class<? extends Data<?>> inputType,
    								Class<? extends Data<?>> outputType) {
        this.name = name;
        this.inputType = inputType;
        this.outputType = outputType;
        this.inputCount = Integer.MAX_VALUE;
        this.outputCount = Integer.MAX_VALUE;
    }

    abstract public <P extends Period> Data<?> evaluate(PeriodicRange<P> range);
    
    public Set<String> getDependencies() {
        return inputStack.stream().flatMap(d -> d.getDependencies().stream()).collect(Collectors.toSet()); 
    }
    
    public boolean isTerminal() {
        return false;
    }
    
    public String toString() {
        return name;
    }

    public String summarize(String prefix) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(prefix).append(toString()).append("\n");
    	inputStack.stream().map(c -> c.summarize(prefix + "\t"))
    			.forEach(s -> sb.append(s).append("\n"));
        return sb.toString();
    }

	public Command clone() {
		try {
			Command clone = (Command) super.clone();
			clone.inputStack = new ArrayDeque<>();
			inputStack.stream().map(Command::clone).forEach(clone.inputStack::addLast);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void addInput(Command c) {
        inputStack.addFirst(c);
    }

    public int inputsNeeded() {
        return inputCount == Integer.MAX_VALUE ? Integer.MAX_VALUE : inputCount - inputStack.size();
    }

	protected void determineOutputCount() {

	}

	public int outputCount() {
		if(outputCount == Integer.MAX_VALUE) {
			determineOutputCount();
		}
		return outputCount;
	}

    public Class<? extends Data<?>> getOutputType() {
        return outputType;
    }
    
    public Class<? extends Data<?>> getInputType() {
        return inputType;
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
