package tech.pinto.function;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

final public class PlaceholderFunction extends Function {

	private final String calledBy;
	
	public PlaceholderFunction(String calledBy) {
		super(Optional.empty(), new LinkedList<>(), new String[]{});
		this.calledBy = calledBy;
		labeller = f -> {
			//System.out.println("Labelling on a " + f.cloneOrNot + " placeholder. And I have " + f.inputStack.size() + " inputs.");
			if(f.inputStack.size() == 0) {
				throw getError();
			} else {
				return f.inputStack.toString();
			}
		};
	}
	
	public void setDelagate(Function function) {
		if(inputStack.size() > 0) {
			throw new RuntimeException("Delagate already set for " + calledBy);
		}
		inputStack.add(function);
	}
	
	public RuntimeException getError() {
		return new RuntimeException("Insufficient inputs for " + calledBy);
	}
	
	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		if(inputStack.isEmpty()) {
			throw getError();
		}
		return inputStack.getFirst().evaluate(range);
	}

	@Override
	public Function getReference() {
		if(inputStack.isEmpty()) {
			throw getError();
		}
		return inputStack.getFirst().getReference();
	}

	@Override
	public int getOutputCount() {
		if(inputStack.isEmpty()) {
			throw getError();
		}
		return inputStack.getFirst().getOutputCount();
	}

	@Override
	public LinkedList<Function> getStack() {
		if(inputStack.isEmpty()) {
			throw getError();
		}
		return inputStack.getFirst().getStack();
	}

	@Override
	public void setLabeller(java.util.function.Function<Function, String> labeller) {
		if(inputStack.isEmpty()) {
			throw getError();
		}
		inputStack.getFirst().setLabeller(labeller);
	}

	@Override
	public Set<String> getDependencies() {
		if(inputStack.isEmpty()) {
			throw getError();
		}
		return inputStack.getFirst().getDependencies();
	}

	@Override
	public void getPlaceholders(LinkedList<PlaceholderFunction> placeholders) {
		placeholders.add(this);
		if(!inputStack.isEmpty()) {
			inputStack.getFirst().getPlaceholders(placeholders);
		}
	}

	@Override
	public void getLeafNodes(LinkedList<Function> leaves) {
		if(!inputStack.isEmpty()) {
			inputStack.getFirst().getLeafNodes(leaves);
		}
	}
	
	

}
