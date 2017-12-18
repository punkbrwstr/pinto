package tech.pinto;

import java.text.NumberFormat;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Column<T,S extends BaseStream<T,S>> implements Cloneable {
	
	protected Optional<PeriodicRange<?>> range = Optional.empty();
	protected Column<?,?>[] inputs;
	protected Function<Column<?,?>[],String> headerFunction = columns -> "";
	final private Function<Column<?,?>[],Function<PeriodicRange<?>,S>> rowsFunction;
	final private Function<Column<?,?>[],Function<NumberFormat,Function<PeriodicRange<?>,Stream<String>>>> rowsAsTextFunction;
	
	public Column(Function<Column<?,?>[], String> headerFunction,
				Function<Column<?,?>[], Function<PeriodicRange<?>, S>> rowsFunction,
				Function<Column<?,?>[], Function<NumberFormat, Function<PeriodicRange<?>, Stream<String>>>> rowsAsTextFunction,
				Column<?,?>... inputs) {
		this.inputs = inputs;
		this.headerFunction = headerFunction;
		this.rowsFunction = rowsFunction;
		this.rowsAsTextFunction = rowsAsTextFunction;
	}
	
	public void setRange(PeriodicRange<?> range) {
		this.range = Optional.of(range);
		for(Column<?,?> c : inputs) {
			c.setRange(range);
		}
	}
	
	public Optional<PeriodicRange<?>> getRange() {
		return range;
	}

	public String getHeader() {
		return headerFunction.apply(inputs);	
	}
	
	public void setHeaderFunction(Function<Column<?,?>[],String> headerFunction) {
		this.headerFunction = headerFunction;
	}
	
	public <P extends Period> S rows() {
		 return rowsFunction.apply(inputs).apply(range.get());
	}

	public <P extends Period> Stream<String> rowsAsText(NumberFormat nf) {
		return rowsAsTextFunction.apply(inputs).apply(nf).apply(range.get());
	}
	
	public <P extends Period> Stream<String> rowsAsText() {
		return rowsAsText(NumberFormat.getInstance());
	}
	
	public Column<?,?>[] getInputs() {
		return inputs;
	}

	@Override
	public String toString() {
		return getHeader();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Column clone() {
		try {
			Column clone = (Column) super.clone();
			clone.inputs = new Column<?,?>[inputs.length];
			for(int i = 0; i < inputs.length; i++) {
				clone.inputs[i] = inputs[i].clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Shouldn't happen implements cloneable.");
		}
	}
	
	public static class OfDoubles extends Column<Double,DoubleStream> {

		public OfDoubles(Function<Column<?, ?>[], String> headerFunction,
				Function<Column<?, ?>[], Function<PeriodicRange<?>, DoubleStream>> rowsFunction,
				Column<?, ?>... inputs) {
			super(headerFunction, rowsFunction, i -> nf -> range -> rowsFunction.apply(i).apply(range).mapToObj(nf::format), inputs);
		}
		
	}
	
	public static class OfDoubleArrays extends Column<DoubleStream,Stream<DoubleStream>> {

		public OfDoubleArrays(Function<Column<?, ?>[], String> headerFunction,
				Function<Column<?, ?>[], Function<PeriodicRange<?>, Stream<DoubleStream>>> rowsFunction,
				Column<?, ?>... inputs) {
			super(headerFunction, rowsFunction, i -> nf -> range -> {
				return rowsFunction.apply(i).apply(range).map(DoubleStream::toArray).map(a -> {
					return "[" + nf.format(a[0]) + (a.length == 1 ? "" : ", ..., " + nf.format(a[a.length-1]) + "]");
				});
			}, inputs);
		}
		
	}
	
	public interface ConstantColumn<T2,S2 extends BaseStream<T2,S2>>  {

		public T2 getValue();
	}
	
	public static class OfConstantDoubles extends OfDoubles implements ConstantColumn<Double,DoubleStream> {
		
		final double d;

		public OfConstantDoubles(double d) {
			super(inputs -> Double.toString(d),
					i -> range -> DoubleStream.iterate(d, r -> d).limit(range.size()),
					new Column<?,?>[] {}
					);
			this.d = d;
		}
		
		public Double getValue() {
			return d;
		}

	}
	
	public static class OfConstantStrings extends Column<String,Stream<String>> implements ConstantColumn<String,Stream<String>> {
		
		final String value;
		final String header;

		public OfConstantStrings(String value, String header) {
			super(inputs -> header,
					i -> range -> Stream.generate(() -> value).limit(range.size()),
					i -> nf -> range -> Stream.generate(() -> value).limit(range.size()),
					new Column<?,?>[] {}
					);
			this.value = value;
			this.header = header;
			headerFunction = inputs -> header;
		}

		public String getValue() {
			return value;
		}

	}
	
}
