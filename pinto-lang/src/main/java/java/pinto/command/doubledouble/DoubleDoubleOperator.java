package pinto.command.doubledouble;

import java.util.ArrayDeque;

import java.util.PrimitiveIterator.OfDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.DoubleStream;

import pinto.command.Command;
import pinto.data.DoubleData;
import pinto.time.Period;
import pinto.time.PeriodicRange;

public class DoubleDoubleOperator extends Command<DoubleStream,DoubleData,DoubleStream,DoubleData> {

	protected final DoubleBinaryOperator operator;
	
	public DoubleDoubleOperator(String name, DoubleBinaryOperator operator) {
		super(name,DoubleData.class, DoubleData.class);
		this.operator = operator;
		inputCount = 2;
		outputCount = 1;
	}

	@Override
	public <P extends Period> ArrayDeque<DoubleData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<DoubleData> output = new ArrayDeque<>();
		ArrayDeque<DoubleData> inputs = getInputData(range);
		DoubleData b = inputs.removeFirst();
		DoubleData a = inputs.removeFirst();
	 	OfDouble bIterator = b.getData().iterator();
		DoubleStream outputStream = a.getData()
				.map(aValue -> operator.applyAsDouble(aValue, bIterator.nextDouble())); 
		output.add(new DoubleData(range, joinWithSpaces(a.getLabel(),b.getLabel(),toString()),outputStream));
		return output;
	}

}
