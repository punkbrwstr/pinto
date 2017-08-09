package tech.pinto;

import java.util.function.Function;
import java.util.stream.DoubleStream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

final public class Column implements Cloneable {

	final private Column[] inputs;
	final private Function<Column[],String> headerFunction;
	final private Function<Column[],Function<PeriodicRange<?>,DoubleStream>> cellsFunction;
	final private boolean header_only;
	
	public Column(Function<Column[],String> headerFunction,
			Function<Column[], Function<PeriodicRange<?>,DoubleStream>> cellsFunction,
				Column... inputs) {
		this.inputs = inputs;
		this.headerFunction = headerFunction;
		this.cellsFunction = cellsFunction;
		this.header_only = false;
	}

	public Column(String header) {
		this.inputs = new Column[]{};
		this.headerFunction = i -> header;
		this.cellsFunction = i -> range -> DoubleStream.iterate(Double.NaN, r -> Double.NaN).limit(range.size());
		this.header_only = true;
	}
	
	public boolean isHeaderOnly() {
		return header_only;
	}
	
	public String getHeader() {
		return headerFunction.apply(inputs);
	}
	
	public <P extends Period> DoubleStream getCells(PeriodicRange<P> range) {
		return cellsFunction.apply(inputs).apply(range);
	}
	
	public Column[] getInputs() {
		return inputs;
	}

	public Function<Column[], String> getHeaderFunction() {
		return headerFunction;
	}

	public Function<Column[], Function<PeriodicRange<?>, DoubleStream>> getSeriesFunction() {
		return cellsFunction;
	}

	@Override
	public String toString() {
		return getHeader();
	}
	
	@Override
	public Column clone() {
		try {
			Column clone = (Column) super.clone();
			for(int i = 0; i < inputs.length; i++) {
				clone.inputs[i] = inputs[i].clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Shouldn't happen implements cloneable.");
		}
	}
	
	
	

}
