package tech.pinto.command.doubledouble;

import java.util.ArrayDeque;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import tech.pinto.command.Command;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class DoubleOperator extends Command<DoubleStream,DoubleData,DoubleStream,DoubleData> {

	protected final DoubleUnaryOperator operator;
	
	public DoubleOperator(String name, DoubleUnaryOperator operator) {
		super(name,DoubleData.class, DoubleData.class);
		this.operator = operator;
		inputCount = 1;
		outputCount = 1;
	}

	@Override
	public <P extends Period> ArrayDeque<DoubleData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<DoubleData> output = new ArrayDeque<>();
		ArrayDeque<DoubleData> inputs = getInputData(range);
		output.add(new DoubleData(range, joinWithSpaces(inputs.peekFirst().getLabel(),toString()),
				inputs.removeFirst().getData().map(operator)));
		return output;
	}

}
