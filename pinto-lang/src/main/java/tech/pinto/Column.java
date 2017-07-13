package tech.pinto;


import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

final public class Column implements Cloneable {

	final private Column[] inputs;
	final private Optional<Function<Column[],String>> headerFunction;
	final private Optional<Function<Column[],Function<PeriodicRange<?>,DoubleStream>>> seriesFunction;
	
	public Column(Column[] inputs, Optional<Function<Column[], String>> headerFunction,
			Optional<Function<Column[], Function<PeriodicRange<?>, DoubleStream>>> seriesFunction) {
		this.inputs = inputs;
		this.headerFunction = headerFunction;
		this.seriesFunction = seriesFunction;
	}

	public Column(Function<Column[],String> headerFunction,
			Function<Column[], Function<PeriodicRange<?>,DoubleStream>> seriesFunction,
				Column... inputs) {
		this.inputs = inputs;
		this.headerFunction = Optional.of(headerFunction);
		this.seriesFunction = Optional.of(seriesFunction);
	}

	public Column(Function<Column[],String> textFunction, Column... inputs) {
		this.inputs = inputs;
		this.headerFunction = Optional.of(textFunction);
		this.seriesFunction = Optional.empty();
	}
	
	public Optional<String> getHeader() {
		return headerFunction.isPresent() ? Optional.of(headerFunction.get().apply(inputs)) : Optional.empty();
	}
	
	public <P extends Period> Optional<DoubleStream> getSeries(PeriodicRange<P> range) {
		return seriesFunction.isPresent() ? Optional.of(seriesFunction.get().apply(inputs).apply(range)) : Optional.empty();
	}
	
	public Column[] getInputs() {
		return inputs;
	}

	public Optional<Function<Column[], String>> getHeaderFunction() {
		return headerFunction;
	}

	public Optional<Function<Column[], Function<PeriodicRange<?>, DoubleStream>>> getSeriesFunction() {
		return seriesFunction;
	}

	@Override
	public String toString() {
		return headerFunction.isPresent() ? headerFunction.get().apply(inputs) : "";
	}
	
	@Override
	public Column clone() {
		Column[] cloneInputs = new Column[inputs.length];
		for(int i = 0; i < inputs.length; i++) {
			cloneInputs[i] = (Column) inputs[i].clone();
		}
		return new Column(cloneInputs,headerFunction,seriesFunction);
	}
	
	
	

}
