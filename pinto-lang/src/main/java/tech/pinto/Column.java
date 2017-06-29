package tech.pinto;


import java.util.function.Function;
import java.util.stream.DoubleStream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

final public class Column implements Cloneable {

	final private Column[] inputs;
	final private Function<Column[],String> textFunction;
	final private Function<Column[],Function<PeriodicRange<?>,DoubleStream>> seriesFunction;

	public Column(Function<Column[],String> textFunction,
			Function<Column[], Function<PeriodicRange<?>,DoubleStream>> seriesFunction,
				Column... inputs) {
		this.inputs = inputs;
		this.textFunction = textFunction;
		this.seriesFunction = seriesFunction;
	}

	public <P extends Period> ColumnValues getValues(PeriodicRange<P> range) {
		return new ColumnValues(range, textFunction.apply(inputs),
				seriesFunction.apply(inputs).apply(range));
	}
	
	public Column[] getInputs() {
		return inputs;
	}

	public Function<Column[], String> getTextFunction() {
		return textFunction;
	}

	public Function<Column[], Function<PeriodicRange<?>, DoubleStream>> getSeriesFunction() {
		return seriesFunction;
	}

	@Override
	public String toString() {
		return textFunction.apply(inputs);
	}
	
	@Override
	public Column clone() {
		Column[] cloneInputs = new Column[inputs.length];
		for(int i = 0; i < inputs.length; i++) {
			cloneInputs[i] = (Column) inputs[i].clone();
		}
		return new Column(textFunction,seriesFunction,cloneInputs);
	}
	
	
	

}
