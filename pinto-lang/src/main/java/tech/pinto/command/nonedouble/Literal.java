package tech.pinto.command.nonedouble;


import java.util.stream.DoubleStream;

import tech.pinto.command.Command;
import tech.pinto.data.DoubleData;
import tech.pinto.data.NoneData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Literal extends Command {
	
	private final double value;

	public Literal(double value) {
		super("literal", NoneData.class, DoubleData.class);
		this.value = value;
		inputCount = 0;
		outputCount = 1;
	}

	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		return new DoubleData(range, toString(), DoubleStream.iterate(value, r -> value).limit(range.size()));
	}
	
	@Override
	public String toString() {
		return Double.toString(value);
	}


}
