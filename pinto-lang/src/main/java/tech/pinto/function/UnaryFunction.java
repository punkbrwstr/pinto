package tech.pinto.function;

import java.util.LinkedList;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class UnaryFunction extends Function {

	java.util.function.Function<Function,java.util.function.Function<PeriodicRange<?>,TimeSeries>> evaluationFunction;

	public UnaryFunction(String name, Function input,
			java.util.function.Function<Function,
				java.util.function.Function<PeriodicRange<?>,TimeSeries>> evaluationFunction) {
		super(name, new LinkedList<>(), new String[]{});
		inputStack.add(input);
		this.evaluationFunction = evaluationFunction;
		outputCount = 1;
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		return evaluationFunction.apply(inputStack.getFirst()).apply(range);
	}

	@Override
	public Function getReference() {
		return this;
	}
	
	@Override
	public UnaryFunction clone() {
		UnaryFunction clone = (UnaryFunction) super.clone();
		return clone;
    }

}
