package tech.pinto;

import java.util.TreeMap;

public interface Window {
	
	public int viewCount();
	public View getView(int i);

	public static interface View {
		
		public double get(int i);
		public int size();
		public boolean reset();
		public boolean returnNa();
		public Array additions();
		public Array subtractions();

	}

	public static interface Array {
		public int size();
		public double get(int i);
		default public Array subArray(int start, int end) {
			return null;
		}
	}

	public static interface Statistic {
		
		public double update(View v);
		
		default public double[] apply(Window w) {
			double[] d = new double[w.viewCount()];
			for(int i = 0; i < d.length; i++) {
				View v = w.getView(i);
				double value = update(v);
				d[i] = v.returnNa() ? Double.NaN : value;
			}
			return d;
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
					return false;
				}

				@Override
				public boolean returnNa() {
					double last = d[size + v - 1];
					return last != last;
				}};
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

				@Override
				public double get(int i) {
					int index = i + offset;
					return index < 0 ? Double.NaN : d[offset];
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
					return v + 1;
				}

				@Override
				public boolean reset() {
					return false;
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

				@Override
				public double get(int i) {
					return d[v];
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
					return false;
				}

				@Override
				public boolean returnNa() {
					return false;
				}};
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
		
	}
	
	public static abstract class Accumulator implements Statistic {
		
		protected double sum = 0;
		protected double sumOfSquares = 0;
		protected double count = 0;

		protected void accumulate(View v) {
			if(v.reset()) {
				sum = 0;
				sumOfSquares = 0;
				count = 0;
			} else {
				Array subtractions = v.subtractions();
				for(int i = 0; i < subtractions.size(); i++) {
					double d = subtractions.get(i);
					if(d == d) {
						count--;
						sum -= d;
						sumOfSquares -= d * d;
					}
				}
			}
			Array additions = v.additions();
			for(int i = 0; i < additions.size(); i++) {
				double d = additions.get(i);
				if(d == d) {
					count++;
					sum += d;
					sumOfSquares += d * d;
				}
			}
		}
	}
	
	public static class Sum extends Accumulator {

		@Override
		public double update(View v) {
			accumulate(v);
			return sum;
		}
		
	}

	public static class Mean extends Accumulator {

		@Override
		public double update(View v) {
			accumulate(v);
			return sum / (double) count;
		}
		
	}

	public static class StandardDeviation extends Accumulator {

		@Override
		public double update(View v) {
			accumulate(v);
			return Math.sqrt((sumOfSquares - sum * sum / (double) count) / ((double)count - 1));
		}
		
	}

	public static class ZScore extends Accumulator {

		@Override
		public double update(View v) {
			accumulate(v);
			return (v.get(v.size()-1) - sum / (double) count) /
					Math.sqrt((sumOfSquares - sum * sum / (double) count) / ((double)count - 1));
		}
		
	}
	
	public static Statistic Change = v -> v.get(v.size()-1) - v.get(0);
	public static Statistic PercentChange = v -> v.get(v.size()-1) / v.get(0) - 1;
	public static Statistic First = v -> v.get(0);
	public static Statistic Last = v -> v.get(v.size()-1);
	
	public static abstract class RankingStatistic implements Statistic {
		
		protected TreeMap<Double,Integer> t = new TreeMap<>();
		
		protected abstract double get();

		@Override
		public double update(View v) {
			if(v.reset()) {
				t.clear();
			} else {
				Array subtractions = v.subtractions();
				for(int i = 0; i < subtractions.size(); i++) {
					double d = subtractions.get(i);
					if(d == d) {
						int count = t.get(d);
						if(count == 1) {
							t.remove(d);
						} else {
							t.put(d, --count);
						}
					}
				}
			}
			Array additions = v.additions();
			for(int i = 0; i < additions.size(); i++) {
				double d = additions.get(i);
				if(d == d) {
					int count = t.containsKey(d) ? t.get(d) : -1;
					t.put(d, ++count);
				}
			}
			return get();
		}
		
	}
	
	public static class Max extends RankingStatistic {
		@Override
		protected double get() {
			return t.lastKey();
		}
	}

	public static class Min extends RankingStatistic {

		@Override
		protected double get() {
			return t.firstKey();
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
	
	public static class SubArray implements Array {
		
		final Array a;
		final int start;
		final int end;
		

		private SubArray(Array a, int start, int end) {
			if(start > end || end > a.size()) {
				throw new ArrayIndexOutOfBoundsException();
			}
			this.a = a;
			this.start = start;
			this.end = end;
		}

		public int size() {
			return end - start;
		}

		public double get(int i) {
			return a.get(i + start);
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
