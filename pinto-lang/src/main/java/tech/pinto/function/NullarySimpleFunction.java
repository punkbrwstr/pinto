package tech.pinto.function;

import java.util.LinkedList;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class NullarySimpleFunction extends Function {

	protected final java.util.function.Function<PeriodicRange<?>, TimeSeries> function;
	
	int referenceCount = 0;

	public NullarySimpleFunction(String name, java.util.function.Function<PeriodicRange<?>, TimeSeries> function) {
		this(name, new LinkedList<>(), function);
	}
	public NullarySimpleFunction(String name, LinkedList<Function> inputStack,
			java.util.function.Function<PeriodicRange<?>, TimeSeries> function) {
		super(name, inputStack, new String[]{});
		this.outputCount = inputStack.size() + 1;
		this.function = function;
	}

	@Override
	public Function getReference() {
		return referenceCount++ == 0 ? this : inputStack.removeFirst();
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		return function.apply(range);
	}
	
	
}
