package tech.pinto;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicity;

public abstract class Column<T> implements Cloneable {
	
	protected Column<?>[] inputs;
	protected Function<Column<?>[],String> headerFunction = columns -> "";
	protected Function<Column<?>[],String> traceFunction = columns -> "";
	final private RowsFunction<T> rowsFunction;
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	
	protected Column(Function<Column<?>[], String> headerFunction, Function<Column<?>[], String> traceFunction,
				RowsFunctionGeneric<T> rowsFunction, Column<?>... inputs) {
		this(headerFunction, traceFunction, (range, columns) -> {
			try {
				return rowsFunction.getRows(range, columns, null);
			} catch(Throwable t) {
				throw new PintoSyntaxException("Error in column \"" + traceFunction.apply(columns) + "\"",t);
			}
		}, inputs);
	}

	protected Column(Function<Column<?>[], String> headerFunction, Function<Column<?>[], String> traceFunction,
				RowsFunction<T> rowsFunction, Column<?>... inputs) {
		this.inputs = inputs;
		this.headerFunction = headerFunction;
		this.traceFunction = traceFunction;
		this.rowsFunction = rowsFunction;
	}
	
	protected abstract <P extends Period<P>> String[] rowsToStrings(T t, NumberFormat nf);

	public String getHeader() {
		return headerFunction.apply(inputs);	
	}

	public String getTrace() {
		return traceFunction.apply(inputs);	
	}
	
	public void setHeader(String header) {
		this.headerFunction = inputs -> header;
		final Function<Column<?>[],String> oldTraceFunction = traceFunction;
		this.traceFunction = inputs -> oldTraceFunction.apply(inputs) + " {" + header + "}";
	}
	
	public <P extends Period<P>> T rows(PeriodicRange<P> range) {
		long start = System.nanoTime();
		 T t = rowsFunction.getRows(range, inputs);
		 log.info("{},  elapsed: {}ms", getTrace(), (System.nanoTime() - start) / 1000000d);
		 return t;
	}

	public <P extends Period<P>> String[] rowsAsStrings(PeriodicRange<P> range, NumberFormat nf) {
		return rowsToStrings(rowsFunction.getRows(range, inputs),nf);
	}

	@Override
	public String toString() {
		return getTrace();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Column clone() {
		try {
			Column clone = (Column) super.clone();
			clone.inputs = new Column<?>[inputs.length];
			for(int i = 0; i < inputs.length; i++) {
				clone.inputs[i] = inputs[i].clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Shouldn't happen implements cloneable.");
		}
	}
	
	@FunctionalInterface
	public interface RowsFunction<T> {
		public T getRows(PeriodicRange<?> range, Column<?>[] columns);
	}
	
	@FunctionalInterface
	public interface RowsFunctionGeneric<T> {
		public <P extends Period<P>> T getRows(PeriodicRange<P> range, Column<?>[] columns, Class<?> clazz);
	}

	public interface ConstantColumn<C> {
		public C getValue();
	}
	
	
	public static <T extends Column<S>,S> T castColumn(Column<?> o, Class<T> clazz) {
		try {
			return clazz.cast(o);
		} catch(ClassCastException cce) {
			throw new PintoSyntaxException("Wrong column type: expected "
					+ clazz.getSimpleName() + " and input is " + o.getClass().getSimpleName());
		}
	}


	public static class OfDoubles extends Column<double[]> {

		public OfDoubles(Function<Column<?>[], String> headerFunction, Function<Column<?>[], String> traceFunction,
				RowsFunction<double[]> rowsFunction, Column<?>... inputs) {
			super(headerFunction, traceFunction, rowsFunction, inputs);
		}

		public OfDoubles(Function<Column<?>[], String> headerFunction, RowsFunction<double[]> rowsFunction, Column<?>... inputs) {
			this(headerFunction, headerFunction, rowsFunction, inputs);
		}

		public OfDoubles(Function<Column<?>[], String> headerFunction, Function<Column<?>[], String> traceFunction,
				RowsFunctionGeneric<double[]> rowsFunction, Column<?>... inputs) {
			super(headerFunction, traceFunction, rowsFunction, inputs);
		}

		public OfDoubles(Function<Column<?>[], String> headerFunction, RowsFunctionGeneric<double[]> rowsFunction, Column<?>... inputs) {
			this(headerFunction, headerFunction, rowsFunction, inputs);
		}

		@Override
		protected <P extends Period<P>> String[] rowsToStrings(double[] d, NumberFormat nf) {
			String[] s = new String[d.length];
			for(int j = 0; j < d.length; j++) {
				s[j] = nf.format(d[j]); 
			}
			return s;
		}

		public <P extends Period<P>> String rowsAsCsv(PeriodicRange<P> range, NumberFormat nf) {
			StringBuilder sb = new StringBuilder();
			double[] d = rows(range);
			for(int j = 0; j < d.length; j++) {
				if(j != 0) {
					sb.append(",");
				}
				sb.append(nf.format(d[j])); 
			}
			return sb.toString();
		}
		
	}
	
	public static class OfConstantDoubles extends OfDoubles implements ConstantColumn<Double> {
		
		final double d;
		
		private static final Function<Double,RowsFunctionGeneric<double[]>> F =
				new Function<Double,RowsFunctionGeneric<double[]>>(){

					@Override
					public RowsFunctionGeneric<double[]> apply(Double t) {
						return new RowsFunctionGeneric<double[]>() {
							@Override
							public <P extends Period<P>> double[] getRows(PeriodicRange<P> range, Column<?>[] columns, Class<?> clazz) {
								double[] a = new double[(int)range.size()];
								Arrays.fill(a, t);
								return a;
							}};
					}};

		public OfConstantDoubles(double d, String header) {
			super(inputs -> header, inputs -> Double.toString(d), F.apply(d));
			this.d = d;
		}

		public OfConstantDoubles(double d) {
			this(d,"c");
		}
		
		public Double getValue() {
			return d;
		}

	}

	public static class OfWindow extends Column<Window<?>> {

		public OfWindow(Function<Column<?>[], String> headerFunction,  Function<Column<?>[], String> traceFunction,
				RowsFunction<Window<?>> rowsFunction, Column<?>... inputs) {
			super(headerFunction, traceFunction, rowsFunction, inputs);
		}

		public OfWindow(Function<Column<?>[], String> headerFunction,  Function<Column<?>[], String> traceFunction,
				RowsFunctionGeneric<Window<?>> rowsFunction, Column<?>... inputs) {
			super(headerFunction, traceFunction, rowsFunction, inputs);
		}

		@Override
		protected <P extends Period<P>> String[] rowsToStrings(Window<?> w, NumberFormat nf) {
			String[] s = new String[w.viewCount()];
			Arrays.fill(s, "window");
			return s;
		}
		
	}

	public static class ObjectConstantColumn<C> extends Column<C[]> implements ConstantColumn<C> {

		private final Supplier<C> c;

		public ObjectConstantColumn(Supplier<C> c, String header, Class<C> clazz, Class<C[]> arrayClazz) {
			super(i -> header, i -> header, (range, columns) -> {
					C[] array =  arrayClazz.cast(Array.newInstance(clazz, (int) range.size()));
					Arrays.fill(array, c.get());
					return array;
				});
			this.c = c;
		}

		public C getValue() {
			return c.get();
		}

		@Override
		protected <P extends Period<P>> String[] rowsToStrings(C[] t, NumberFormat nf) {
			String[] s = new String[t.length];
			for(int i = 0; i < t.length; i++) {
				s[i] = t[i].toString();
			}
			return s;
		}
	}
	
	public static class OfConstantStrings extends ObjectConstantColumn<String> {
		
		public OfConstantStrings(String value) {
			this(() -> value,"string");
		}

		public OfConstantStrings(String value, String header) {
			this(() -> value, header);
		}

		public OfConstantStrings(Supplier<String> value, String header) {
			super(value, header, String.class, String[].class);
		}

	}

	public static class OfConstantDates extends ObjectConstantColumn<LocalDate> {

		public OfConstantDates(LocalDate value) {
			super(() -> value,"date", LocalDate.class, LocalDate[].class);
		}

		public OfConstantDates(Supplier<LocalDate> value) {
			super(value,"date", LocalDate.class, LocalDate[].class);
		}

	}

	@SuppressWarnings("rawtypes")
	public static class OfConstantPeriodicities extends ObjectConstantColumn<Periodicity> {
		
		public OfConstantPeriodicities(Periodicity value) {
			super(() -> value, "periodicity", Periodicity.class, Periodicity[].class);
		}

	}
	
}
