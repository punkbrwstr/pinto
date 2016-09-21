package tech.pinto.command.anyany;

import tech.pinto.command.Command;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Duplicate extends Command {
	

	public Duplicate() {
		super("dup", AnyData.class, AnyData.class);
		inputCount = Integer.MAX_VALUE;
		outputCount = Integer.MAX_VALUE;
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size() * 2;
        inputStack.stream().map(Command::clone).forEach(inputStack::addLast);
	}
	
	@Override public Command getReference() {
		return inputStack.removeFirst();
	}




	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		// never gets called bc it passes on references to inputs
		throw new UnsupportedOperationException();
	}

}
