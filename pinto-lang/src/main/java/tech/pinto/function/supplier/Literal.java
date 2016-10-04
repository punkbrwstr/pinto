package tech.pinto.function.supplier;


import java.util.stream.DoubleStream;

import tech.pinto.TimeSeries;
import tech.pinto.function.NullarySimpleFunction;

public class Literal extends NullarySimpleFunction {
	
	private final double value;

	public Literal(double value) {
		super("literal", range ->  new TimeSeries(range, "literal", DoubleStream.iterate(value, r -> value).limit(range.size())));
		this.value = value;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

}
