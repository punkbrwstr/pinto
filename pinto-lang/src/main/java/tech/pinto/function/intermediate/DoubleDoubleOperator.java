package tech.pinto.function.intermediate;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class DoubleDoubleOperator extends Function {

	protected final DoubleBinaryOperator operator;
	protected Function fixed;
	protected final HashMap<PeriodicRange<?>, TimeSeries> fixedData = new HashMap<>();
	
	public DoubleDoubleOperator(String name, LinkedList<Function> inputs, DoubleBinaryOperator operator) {
		super(name,inputs);
		this.operator = operator;
		outputCount = inputStack.size() - 1;
		if(inputStack.size() != 2) {
			throw new IllegalArgumentException("not enough inputs for " + name);
		}
		fixed = inputStack.removeFirst(); 
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		if(!fixedData.containsKey(range)) {
			fixedData.put(range, fixed.evaluate(range));
		}
		TimeSeries a = fixedData.get(range).clone();
		TimeSeries b = inputStack.removeFirst().evaluate(range);
	 	OfDouble bIterator = b.stream().iterator();
		DoubleStream outputStream = a.stream()
				.map(aValue -> operator.applyAsDouble(aValue, bIterator.nextDouble())); 
		return new TimeSeries(range, joinWithSpaces(a.getLabel(),b.getLabel(),toString()),outputStream);
	}

	@Override
	public Function getReference() {
		return this;
	}

	@Override
	public DoubleDoubleOperator clone() {
		DoubleDoubleOperator clone = (DoubleDoubleOperator) super.clone();
		clone.fixed = fixed.clone();
		return clone;
	}
	
	public static Supplier<FunctionHelp> getHelp(String name, String desc) {
		return () -> new FunctionHelp.Builder(name)
				.inputs("double<sub>1</sub>, double<sub>2</sub>")
				.outputs("double")
				.description("Binary double operator for " + desc + ".")
				.build();
	}


}
