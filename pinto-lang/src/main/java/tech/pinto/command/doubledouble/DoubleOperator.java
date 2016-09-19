package tech.pinto.command.doubledouble;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import tech.pinto.command.Command;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class DoubleOperator extends Command {

	protected final DoubleUnaryOperator operator;
	
	public DoubleOperator(String name, DoubleUnaryOperator operator) {
		super(name,DoubleData.class, DoubleData.class);
		this.operator = operator;
		inputCount = 1;
		outputCount = 1;
	}

	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		DoubleData d = (DoubleData) inputStack.removeFirst().evaluate(range);
		return new DoubleData(range, joinWithSpaces(d.getLabel(),toString()), d.getData().map(operator));
	}

}
