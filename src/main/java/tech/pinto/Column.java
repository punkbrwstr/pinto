package tech.pinto;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.function.Function;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Column<T> implements Cloneable {
	
	protected final Class<T> type;
	protected Column<?>[] inputs;
	protected Function<Column<?>[],String> headerFunction = columns -> "";
	protected Function<Column<?>[],String> traceFunction = columns -> "";
	final private RowsFunction<T> rowsFunction;
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	

	public Column(Class<T> type, String header, T rowsFunction) {
		this(type,header, header, rowsFunction);
	}

	public Column(Class<T> type, String header, String trace, T rowsFunction) {
		this(type, i -> header, i -> trace, (r,i) -> rowsFunction);
	}

	public Column(Class<T> type, Function<Column<?>[], String> headerFunction, 
				RowsFunction<T> rowsFunction, Column<?>... inputs) {
		this(type, headerFunction, headerFunction, rowsFunction, inputs);

	}

	public Column(Class<T> type, Function<Column<?>[], String> headerFunction, Function<Column<?>[], String> traceFunction,
				RowsFunction<T> rowsFunction, Column<?>... inputs) {
		this.type = type;
		this.inputs = inputs;
		this.headerFunction = headerFunction;
		this.traceFunction = traceFunction;
		this.rowsFunction = rowsFunction;
	}
	
	public String getHeader() {
		return headerFunction.apply(inputs);	
	}

	public String getTrace() {
		return traceFunction.apply(inputs);	
	}
	
	public Class<T> getType() {
		return type;
	}
	
	public void setHeader(String header) {
		this.headerFunction = inputs -> header;
		final Function<Column<?>[],String> oldTraceFunction = traceFunction;
		this.traceFunction = inputs -> oldTraceFunction.apply(inputs) + " {" + header + "}";
	}
	
	public <P extends Period<P>> T rows(PeriodicRange<P> range) {
		//long start = System.nanoTime();
		 T t = rowsFunction.getRows(range, inputs);
		//log.info("{},  elapsed: {}ms", getTrace(), (System.nanoTime() - start) / 1000000d);
		 return t;
	}

	public <P extends Period<P>> String[] rowsAsStrings(PeriodicRange<P> range, NumberFormat nf) {
		String[] s = new String[(int)range.size()];
		if(type.equals(double[].class)) {
			double[] d = double[].class.cast(rows(range));
			for(int j = 0; j < s.length; j++) {
				s[j] = nf.format(d[j]); 
			}
		} else if(type.isArray()) {
			Object[] d = Object[].class.cast(rows(range));
			for(int j = 0; j < s.length; j++) {
				s[j] = d[j].toString();
			}
		} else {
			Arrays.fill(s, ((Object) rows(range)).toString());
		}
		return s;
	}
	
	@SuppressWarnings("unchecked")
	public <C> Column<C> cast(Class<C> clazz) {
		if(clazz.isAssignableFrom(type)) {
			return (Column<C>) this;
		} else if(clazz.equals(double[].class) && type.equals(Double.class)) {
			return (Column<C>) new Column<double[]>(double[].class, headerFunction, traceFunction,
				(range, inputs) ->  {
					double[] d = new double[(int)range.size()];
					Arrays.fill(d, (Double)rows(null));
					return d;
				});
		}
		throw new IllegalArgumentException("Unable to cast column " + type.toGenericString() +
				" to " + clazz.toGenericString());
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
	public interface RowsFunctionGeneric<T> extends RowsFunction<T> {
		public <P extends Period<P>> T getRows(PeriodicRange<P> range, Column<?>[] columns, Class<?> clazz);

		default public T getRows(PeriodicRange<?> range, Column<?>[] columns) {
			return getRows(range, columns, null);
		}
	}
}
