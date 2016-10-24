package tech.pinto.function;

import java.util.LinkedList;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

final public class LambdaFunction extends Function {

	final private java.util.function.Function<LinkedList<Function>,
			java.util.function.Function<PeriodicRange<?>,DoubleStream>> evaluationFunction;
	final int inputsNeeded;

	public LambdaFunction(java.util.function.Function<Function,String> labeller,
			java.util.function.Function<LinkedList<Function>,
				java.util.function.Function<PeriodicRange<?>,DoubleStream>> evaluationFunction,
				Function...functions) {
		this(labeller, evaluationFunction,functions.length, linkedListOf(functions));

	}

	public LambdaFunction(java.util.function.Function<Function,String> labellerFunction, 
			java.util.function.Function<LinkedList<Function>,
				java.util.function.Function<PeriodicRange<?>,DoubleStream>> evaluationFunction,
				int inputsNeeded, LinkedList<Function> inputs) {
		super(Optional.empty(), inputs, new String[]{});
		this.evaluationFunction = evaluationFunction;
		this.inputsNeeded = inputsNeeded;
		this.labeller = labellerFunction;
	}
	
	public int getOutputCount() {
		return inputStack.size() - inputsNeeded + 1;
	}
	

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		return new TimeSeries(range,toString(),evaluationFunction.apply(inputStack).apply(range));
	}

	@Override
	public Function getReference() {
		return inputStack.size() > inputsNeeded ? inputStack.removeLast() : this;
	}
	
	
	private static LinkedList<Function> linkedListOf(Function... f) {
		return Stream.of(f).collect(Collectors.toCollection(LinkedList::new));
	}

}
