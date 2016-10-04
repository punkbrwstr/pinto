package tech.pinto.function;

import java.util.LinkedList;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class IntermediateFunction extends Function {

	public IntermediateFunction(String name, LinkedList<Function> inputStack, String... arguments) {
		super(name, inputStack, arguments);
	}

	abstract public Function getReference();

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Function clone() {
		throw new UnsupportedOperationException();
	}


}
