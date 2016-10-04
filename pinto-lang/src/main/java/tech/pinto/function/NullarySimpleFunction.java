package tech.pinto.function;

import java.util.LinkedList;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class NullarySimpleFunction extends Function {

	protected final java.util.function.Function<PeriodicRange<?>, TimeSeries> function;

	public NullarySimpleFunction(String name, java.util.function.Function<PeriodicRange<?>, TimeSeries> function) {
		super(name, new LinkedList<>(), new String[]{});
		this.outputCount = 1;
		this.function = function;
	}

	@Override
	public Function getReference() {
		return this;
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		return function.apply(range);
	}
	
	
}
