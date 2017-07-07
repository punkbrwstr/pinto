package tech.pinto;


import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

final public class Column implements Cloneable {

	final private Column[] inputs;
	final private Function<Column[],Optional<String>> textFunction;
	final private Function<Column[],Function<PeriodicRange<?>,Optional<DoubleStream>>> seriesFunction;
	
	private Column(Column[] inputs, Function<Column[], Optional<String>> textFunction,
			Function<Column[], Function<PeriodicRange<?>, Optional<DoubleStream>>> seriesFunction) {
		this.inputs = inputs;
		this.textFunction = textFunction;
		this.seriesFunction = seriesFunction;
	}

	public Column(Function<Column[],String> textFunction,
			Function<Column[], Function<PeriodicRange<?>,DoubleStream>> seriesFunction,
				Column... inputs) {
		this.inputs = inputs;
		this.textFunction = c -> Optional.of(textFunction.apply(c));
		this.seriesFunction = c -> r -> Optional.of(seriesFunction.apply(c).apply(r));
	}

	public Column(Function<Column[],String> textFunction, Column... inputs) {
		this.inputs = inputs;
		this.textFunction = c -> Optional.of(textFunction.apply(c));
		this.seriesFunction = c -> r -> Optional.empty();
	}

	public <P extends Period> ColumnValues getValues(PeriodicRange<P> range) {
		return new ColumnValues(textFunction.apply(inputs),
				seriesFunction.apply(inputs).apply(range));
	}
	
	public Column[] getInputs() {
		return inputs;
	}

	public Function<Column[], Optional<String>> getTextFunction() {
		return textFunction;
	}

	public Function<Column[], Function<PeriodicRange<?>, Optional<DoubleStream>>> getSeriesFunction() {
		return seriesFunction;
	}

	@Override
	public String toString() {
		return textFunction.apply(inputs).orElse("");
	}
	
	@Override
	public Column clone() {
		Column[] cloneInputs = new Column[inputs.length];
		for(int i = 0; i < inputs.length; i++) {
			cloneInputs[i] = (Column) inputs[i].clone();
		}
		return new Column(cloneInputs,textFunction,seriesFunction);
	}
	
	
	

}
