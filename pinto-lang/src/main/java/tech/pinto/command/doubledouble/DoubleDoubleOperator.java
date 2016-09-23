package tech.pinto.command.doubledouble;


import java.util.PrimitiveIterator.OfDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import tech.pinto.command.Command;
import tech.pinto.command.CommandHelp;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class DoubleDoubleOperator extends Command {

	protected final DoubleBinaryOperator operator;
	
	public DoubleDoubleOperator(String name, DoubleBinaryOperator operator) {
		super(name,DoubleData.class, DoubleData.class);
		this.operator = operator;
		inputCount = 2;
		outputCount = 1;
	}

	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		if(inputStack.size() != 2) {
			throw new IllegalArgumentException("not enough inputs for binary operator");
		}
		DoubleData a = (DoubleData) inputStack.removeFirst().evaluate(range);
		DoubleData b = (DoubleData) inputStack.removeFirst().evaluate(range);
	 	OfDouble bIterator = b.getData().iterator();
		DoubleStream outputStream = a.getData()
				.map(aValue -> operator.applyAsDouble(aValue, bIterator.nextDouble())); 
		return new DoubleData(range, joinWithSpaces(a.getLabel(),b.getLabel(),toString()),outputStream);
	}
	
	public static Supplier<CommandHelp> getHelp(String name, String desc) {
		return () -> new CommandHelp.Builder(name)
				.inputs("double<sub>1</sub>, double<sub>2</sub>")
				.outputs("double")
				.description("Binary double operator for " + desc + ".")
				.build();
	}

}
