package tech.pinto.function;

import java.util.LinkedList;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class NullaryReferenceFunction extends Function {


	public NullaryReferenceFunction(String name, LinkedList<Function> inputs, String...args) {
		super(name, inputs, args);
		outputCount = inputs.size();
	}

	abstract protected Function supplyReference(); 
	abstract protected int myOutputCount(); 

	@Override
	public Function getReference() {
		return !inputStack.isEmpty() ? inputStack.removeFirst() : supplyReference();
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getOutputCount() {
		return outputCount + myOutputCount();
	}


	
}
