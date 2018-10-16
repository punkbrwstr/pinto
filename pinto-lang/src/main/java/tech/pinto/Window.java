package tech.pinto;

import java.time.LocalDate;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.data.MatrixType;

import tech.pinto.time.PeriodicRange;

public interface Window {
	
	public int viewCount();
	public View getView(int i);
	public void apply(DoubleUnaryOperator duo);
	public void apply(DoubleBinaryOperator duo, Window other);


	public static interface View extends DMatrix {
		
		public double get(int i);
		public int size();
		public boolean reset();
		public boolean returnNa();
		public Array additions();
		public Array subtractions();
		
		default public DMatrixRMaj toMatrix() {
			return new DMatrixRMaj(this);
		}
		
		@Override
		default int getNumRows() {
			return size();
		}
		@Override
		default int getNumCols() {
			return 1;
		}
		@Override
		default MatrixType getType() {
			return MatrixType.DDRM;
		}
		@Override
		default double get(int row, int col) {
			return get(row);
		}
		@Override
		default double unsafe_get(int row, int col) {
			return get(row);
		}
		@Override
		default int getNumElements() {
			return size();
		}

		@Override default <T extends Matrix> T copy() { throw new UnsupportedOperationException(); }
		@Override default <T extends Matrix> T createLike() { throw new UnsupportedOperationException(); }
		@Override default void set(Matrix original) { throw new UnsupportedOperationException(); }
		@Override default void print() { throw new UnsupportedOperationException(); }
		@Override default void print(String format) { throw new UnsupportedOperationException(); }
		@Override default void set(int row, int col, double val) { throw new UnsupportedOperationException(); }
		@Override default void unsafe_set(int row, int col, double val) { throw new UnsupportedOperationException(); }

	}

	public static interface Array {
		public int size();
		public double get(int i);
		default public Array subArray(int start, int end) {
			return null;
		}
	}
	
	public static class Rolling implements Window {
		
		private final double[] d;
		private final int size;
		
		public Rolling(double[] d, int size) {
			this.d = d;
			this.size = size;
		}

		@Override
		public int viewCount() {
			return d.length - size + 1;
		}

		@Override
		public View getView(int v) {
			return new View() {
				private static final long serialVersionUID = 1L;

				@Override
				public double get(int i) {
					return d[i + v];
				}

				@Override
				public Array additions() {
					return v == 0 ? new BaseArray(d, 0, size) : new BaseArray(d, size + v - 1, size + v);
				}

				@Override
				public Array subtractions() {
					return v == 0 ? new EmptyArray() : new BaseArray(d, v - 1, v);
				}

				@Override
				public int size() {
					return size;
				}

				@Override
				public boolean reset() {
					return v == 0;
				}

				@Override
				public boolean returnNa() {
					double last = d[size + v - 1];
					return last != last;
				}};
		}

		@Override
		public void apply(DoubleUnaryOperator duo) {
			for(int i = 0; i < d.length; i++) {
				d[i] = duo.applyAsDouble(d[i]);
			}
		}

		@Override
		public void apply(DoubleBinaryOperator dbo, Window other) {
			if(!other.getClass().equals(Rolling.class)) {
				throw new IllegalArgumentException("Incompatible window types.");
			} else if (((Rolling) other).d.length != d.length || ((Rolling) other).size != size) {
				throw new IllegalArgumentException("Incompatible window dimensions.");
			}
			for(int i = 0; i < d.length; i++) {
				d[i] = dbo.applyAsDouble(d[i], ((Rolling)other).d[i]);
			}
		}
		
	}
	

	public static class Expanding implements Window {
		
		private final double[] d;
		private final int offset;
		
		public Expanding(double[] d, int offset) {
			this.d = d;
			this.offset = offset;
		}

		@Override
		public int viewCount() {
			return d.length - offset;
		}

		@Override
		public View getView(int v) {
			return new View() {
				private static final long serialVersionUID = 1L;

				@Override
				public double get(int i) {
					return d[i];
				}

				@Override
				public Array additions() {
					return v + offset < 0 ? new EmptyArray() :
							v == 0 ? new BaseArray(d, 0, offset + 1) :
								new BaseArray(d, v + offset, v + offset + 1);
				}

				@Override
				public Array subtractions() {
					return new EmptyArray();
				}

				@Override
				public int size() {
					return Math.max(0, v + 1 + offset);
				}

				@Override
				public boolean reset() {
					return v == 0;
				}

				@Override
				public boolean returnNa() {
					if(v + offset < 0) {
						return true;
					}
					double last = d[v + offset];
					return last != last;
				}};
		}

		@Override
		public void apply(DoubleUnaryOperator duo) {
			for(int i = 0; i < d.length; i++) {
				d[i] = duo.applyAsDouble(d[i]);
			}
		}

		@Override
		public void apply(DoubleBinaryOperator dbo, Window other) {
			if(!other.getClass().equals(Expanding.class)) {
				throw new IllegalArgumentException("Incompatible window types.");
			} else if (((Expanding) other).d.length != d.length || ((Expanding) other).offset != offset) {
				throw new IllegalArgumentException("Incompatible window dimensions.");
			}
			for(int i = 0; i < d.length; i++) {
				d[i] = dbo.applyAsDouble(d[i], ((Expanding)other).d[i]);
			}
		}
		
	}

	public static class ReverseExpanding implements Window {
		
		private final double[] d;
		
		public ReverseExpanding(double[] d) {
			this.d = d;
		}

		@Override
		public int viewCount() {
			return d.length;
		}

		@Override
		public View getView(int v) {
			return new View() {
				private static final long serialVersionUID = 1L;

				@Override
				public double get(int i) {
					return d[v + i];
				}

				@Override
				public Array additions() {
					return v == 0 ? new BaseArray(d, 0, d.length) : new EmptyArray();
				}

				@Override
				public Array subtractions() {
					return v == 0 ? new EmptyArray() : new BaseArray(d, v - 1, v);
				}

				@Override
				public int size() {
					return d.length - v;
				}

				@Override
				public boolean reset() {
					return v == 0;
				}

				@Override
				public boolean returnNa() {
					return false;
				}};
		}

		@Override
		public void apply(DoubleUnaryOperator duo) {
			for(int i = 0; i < d.length; i++) {
				d[i] = duo.applyAsDouble(d[i]);
			}
		}

		@Override
		public void apply(DoubleBinaryOperator dbo, Window other) {
			if(!other.getClass().equals(ReverseExpanding.class)) {
				throw new IllegalArgumentException("Incompatible window types.");
			} else if (((ReverseExpanding) other).d.length != d.length) {
				throw new IllegalArgumentException("Incompatible window dimensions.");
			}
			for(int i = 0; i < d.length; i++) {
				d[i] = dbo.applyAsDouble(d[i], ((ReverseExpanding)other).d[i]);
			}
		}
		
	}
	
	public static class Cross implements Window {
		
		final double[][] d;
		
		public Cross(double[][] d) {
			this.d = d;
		}

		@Override
		public int viewCount() {
			return d.length < 1 ? 0 : d[0].length;
		}

		@Override
		public View getView(int v) {
			return new View() {
				private static final long serialVersionUID = 1L;

				@Override
				public double get(int i) {
					return d[i][v];
				}

				@Override
				public int size() {
					return d.length;
				}

				@Override
				public boolean reset() {
					return true;
				}

				@Override
				public boolean returnNa() {
					return false;
				}

				@Override
				public Array additions() {
					return new CrossArray(d, v);
				}

				@Override
				public Array subtractions() {
					return new EmptyArray();
				}};
		}

		@Override
		public void apply(DoubleUnaryOperator duo) {
			for(int i = 0; i < d.length; i++) {
				for(int j = 0; i < d.length; i++) {
					d[i][j] = duo.applyAsDouble(d[i][j]);
				}
			}
		}

		@Override
		public void apply(DoubleBinaryOperator duo, Window other) {
			if(!other.getClass().equals(Cross.class)) {
				throw new IllegalArgumentException("Incompatible window types.");
			} else if (((Cross) other).d.length != d.length || (d.length > 0 && ((Cross) other).d[0].length != d[0].length)) {
				throw new IllegalArgumentException("Incompatible window dimensions.");
			}
			for(int i = 0; i < d.length; i++) {
				for(int j = 0; i < d.length; i++) {
					d[i][j] = duo.applyAsDouble(d[i][j], ((Cross)other).d[i][j]);
				}
			}
		}
		
	}

	public static class Downsample implements Window {
		
		final double[] data;
		final List<LocalDate> dates;
		final PeriodicRange<?> dataRange;	

		public Downsample(double[] data, List<LocalDate> dates, PeriodicRange<?> dataRange) {
			this.data = data;
			this.dates = dates;
			this.dataRange = dataRange;
		}

		@Override
		public int viewCount() {
			return dates.size();
		}

		@Override
		public View getView(int v) {
			return new View() {
				private static final long serialVersionUID = 1L;

				int start = v == 0 ? 0 : (int) dataRange.indexOf(dates.get(v-1)) + 1; // inclusive
				int end = (int) dataRange.indexOf(dates.get(v)) + 1; // exclusive

				@Override
				public double get(int i) {
					return data[start + i];
				}

				@Override
				public int size() {
					return end - start + 1;
				}

				@Override
				public boolean reset() {
					return true;
				}

				@Override
				public boolean returnNa() {
					return false;
				}

				@Override
				public Array additions() {
					return new BaseArray(data, start, end);
				}

				@Override
				public Array subtractions() {
					return new EmptyArray();
				}};
		}

		@Override
		public void apply(DoubleUnaryOperator duo) {
			for(int i = 0; i < data.length; i++) {
				data[i] = duo.applyAsDouble(data[i]);
			}
		}

		@Override
		public void apply(DoubleBinaryOperator dbo, Window other) {
			if(!other.getClass().equals(Downsample.class)) {
				throw new IllegalArgumentException("Incompatible window types.");
			} else if (((Downsample) other).data.length != data.length || ((Downsample) other).dataRange != dataRange) {
				throw new IllegalArgumentException("Incompatible window dimensions.");
			}
			for(int i = 0; i < data.length; i++) {
				data[i] = dbo.applyAsDouble(data[i], ((Downsample)other).data[i]);
			}
		}
	}
	
	public static class ResetOnNa implements Window {
		
		private final Window wrapped;
		

		public ResetOnNa(Window wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public int viewCount() {
			return wrapped.viewCount();
		}

		@Override
		public View getView(int i) {
			final var v = wrapped.getView(i);
			return new View() {
				private static final long serialVersionUID = 1L;

				@Override
				public double get(int i) {
					return v.get(i);
				}

				@Override
				public int size() {
					return v.size();
				}

				@Override
				public boolean reset() {
					return v.reset() || v.returnNa();
				}

				@Override
				public boolean returnNa() {
					return v.returnNa();
				}

				@Override
				public Array additions() {
					return v.additions();
				}

				@Override
				public Array subtractions() {
					return v.subtractions();
				}};
		}

		@Override
		public void apply(DoubleUnaryOperator duo) {
			wrapped.apply(duo);
		}

		@Override
		public void apply(DoubleBinaryOperator duo, Window other) {
			wrapped.apply(duo,other);
		}
		
	}

	public static class EmptyArray implements Array {

		@Override
		public int size() {
			return 0;
		}

		@Override
		public double get(int i) {
			return Double.NaN;
		}
		
	}
		
	public static class BaseArray implements Array {
		
		double[] d;
		final int start;
		final int end;

		public BaseArray(double[] d, int start, int end) {
			if(start > end || end > d.length) {
				throw new ArrayIndexOutOfBoundsException();
			}
			this.d = d;
			this.start = start;
			this.end = end;
		}

		@Override
		public int size() {
			return end - start;
		}

		@Override
		public double get(int i) {
			return d[i + start];
		}
		
	}

	public static class CrossArray implements Array {
		
		final double[][] d;
		final int row;
		

		private CrossArray(double[][] d, int row) {
			if(row >= d[0].length) {
				throw new ArrayIndexOutOfBoundsException("Cross array row " + row + " >= length " + d.length);
			}
			this.d = d;
			this.row = row;
		}

		public int size() {
			return d.length;
		}

		public double get(int i) {
			return d[i][row];
		}
	}
	
}
