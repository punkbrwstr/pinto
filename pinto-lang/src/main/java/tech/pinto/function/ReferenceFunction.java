package tech.pinto.function;

import java.util.LinkedList;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class ReferenceFunction extends Function {

	public ReferenceFunction(String name, LinkedList<Function> inputStack, String... arguments) {
		super(name, inputStack, arguments);
	}

	abstract public Function getReference();

	@Override
	final public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		throw new UnsupportedOperationException();
	}
	
}
