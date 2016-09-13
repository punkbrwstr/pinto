package pinto.command.nonedouble;

import java.util.ArrayDeque;

import java.util.stream.DoubleStream;


import pinto.command.Command;
import pinto.data.DoubleData;
import pinto.data.NoneData;
import pinto.time.Period;
import pinto.time.PeriodicRange;

public class Literal extends Command<Object,NoneData,DoubleStream,DoubleData> {
	
	private final double value;

	public Literal(double value) {
		super("literal", NoneData.class, DoubleData.class);
		this.value = value;
		inputCount = 0;
		outputCount = 1;
	}

	@Override
	public <P extends Period> ArrayDeque<DoubleData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<DoubleData> output = new ArrayDeque<>();
		output.addFirst(new DoubleData(range, toString(), DoubleStream.iterate(value, r -> value).limit(range.size())));
		return output;
	}
	
	@Override
	public String toString() {
		return Double.toString(value);
	}


}
