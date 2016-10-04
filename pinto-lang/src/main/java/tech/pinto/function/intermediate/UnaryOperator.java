package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.function.DoubleUnaryOperator;

import tech.pinto.TimeSeries;
import tech.pinto.function.Function;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class UnaryOperator extends Function {

	protected final DoubleUnaryOperator operator;
	
	public UnaryOperator(String name, LinkedList<Function> inputs, DoubleUnaryOperator operator) {
		super(name,inputs);
		this.operator = operator;
		outputCount = inputStack.size();
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		TimeSeries d = (TimeSeries) inputStack.removeFirst().evaluate(range);
		return new TimeSeries(range, joinWithSpaces(d.getLabel(),toString()), d.stream().map(operator));
	}

	@Override
	public Function getReference() {
		return this;
	}

}
