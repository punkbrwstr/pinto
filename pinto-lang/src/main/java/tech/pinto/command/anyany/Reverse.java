package tech.pinto.command.anyany;

import tech.pinto.command.Command;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Reverse extends Command {

	public Reverse() {
		super("rev", AnyData.class, AnyData.class);
		inputCount = Integer.MAX_VALUE;
		outputCount = Integer.MAX_VALUE;
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size();
	}

	@Override public Command getReference() {
		return inputStack.removeLast();
	}

	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		// never gets called bc it passes on references to inputs
		throw new UnsupportedOperationException();
	}

}
