package tech.pinto;

import java.util.Optional;

import tech.pinto.Window.Array;
import tech.pinto.Window.View;
import tech.pinto.tools.AVLTree;

public interface Statistic {
	
	public double update(View v);
	
	default public double[] apply(Window w) {
		double[] d = new double[w.viewCount()];
		for(int i = 0; i < d.length; i++) {
			View v = w.getView(i);
			d[i] = update(v);
			if(v.returnNa()) {
				d[i] = Double.NaN;
			}
		}
		return d;
	}
	
	public static abstract class Accumulator implements Statistic {
		
		protected double sum = 0;
		protected double sumOfSquares = 0;
		protected double count = 0;
		protected double product = 1;
		protected boolean clearOnNan = false;
		
		private Accumulator(boolean clearOnNan) {
			this.clearOnNan = clearOnNan;
		}

		protected void accumulate(View v) {
			if(v.reset()) {
				sum = 0;
				sumOfSquares = 0;
				count = 0;
				product = 1;
			} else {
				Array subtractions = v.subtractions();
				for(int i = 0; i < subtractions.size(); i++) {
					double d = subtractions.get(i);
					if(d == d) {
						count--;
						sum -= d;
						sumOfSquares -= d * d;
						product /= d;
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
					product *= d;
				} else if(clearOnNan) {
					sum = 0;
					sumOfSquares = 0;
					count = 0;
					product = 1;
				}
			}
		}
	}
	
	public static interface PairStatistic {
		
		public double update(View v1, View v2);
		
		default public double[] apply(Window w1, Window w2) {
			double[] d = new double[w1.viewCount()];
			for(int i = 0; i < d.length; i++) {
				View v1 = w1.getView(i);
				View v2 = w2.getView(i);
				d[i] = update(v1,v2);
				if(v1.returnNa() || v2.returnNa()) {
					d[i] = Double.NaN;
				}
			}
			return d;
		}
	}

	public static class PairCorrelation extends PairCovariance {
		private StandardDeviation sd1, sd2;

		public PairCorrelation(boolean clearOnNan) {
			super(clearOnNan);
			sd1 = new StandardDeviation(clearOnNan);
			sd2 = new StandardDeviation(clearOnNan);
		}

		@Override
		public double update(View v1, View v2) {
			return super.update(v1, v2) / (sd1.update(v1) * sd2.update(v2));
		}
	}

	public static class PairCovariance implements PairStatistic {
		
		double sumX,sumY,count,c = 0;
		protected boolean clearOnNan = false;
		
		public PairCovariance(boolean clearOnNan) {
			this.clearOnNan = clearOnNan;
		}

		@Override
		public double update(View v1, View v2) {
			if(v1.reset() || v2.reset()) {
				sumX = 0;
				sumY = 0;
				count = 0;
				c = 0;
			} else {
				Array subtractions1 = v1.subtractions();
				Array subtractions2 = v2.subtractions();
				for(int i = 0; i < subtractions1.size(); i++) {
					double x = subtractions1.get(i);
					double y = subtractions2.get(i);
					if(x == x && y == y) {
						double prevMeanY = sumY / count;
						count--;
						sumX -= x;
						sumY -= y;
						double meanX = sumX / count;
						c -=  (x - meanX) * (y - prevMeanY);
					}
				}
			}
			Array additions1 = v1.additions();
			Array additions2 = v2.additions();
			for(int i = 0; i < additions1.size(); i++) {
				double x = additions1.get(i);
				double y = additions2.get(i);
				if(x == x &&  y == y) {
					double prevMeanX = count > 0 ? sumX / count : 0;
					count++;
					sumX += x;
					sumY += y;
					double meanY = sumY / count;
					c +=  (x - prevMeanX) * (y - meanY);
				} else if(clearOnNan) {
					sumX = 0;
					sumY = 0;
					count = 0;
					c = 0;
				}
			}
			return c / (count - 1.0d);
		}
	}
	
	public static class Sum extends Accumulator {
		
		public Sum(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		public double update(View v) {
			accumulate(v);
			return sum;
		}
		
	}

	public static class Product extends Accumulator {

		public Product(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		public double update(View v) {
			accumulate(v);
			return product;
		}
		
	}

	public static class Mean extends Accumulator {

		public Mean(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		public double update(View v) {
			accumulate(v);
			return sum / (double) count;
		}
		
	}

	public static class StandardDeviation extends Variance {

		public StandardDeviation(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		public double update(View v) {
			return Math.sqrt(super.update(v));
		}
	}

	public static class Variance extends Accumulator {

		public Variance(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		public double update(View v) {
			accumulate(v);
			return (sumOfSquares - sum * sum / (double) count) / ((double)count - 1);
		}
		
	}

	public static class ZScore extends Accumulator {

		public ZScore(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		public double update(View v) {
			accumulate(v);
			double denom = Math.sqrt((sumOfSquares - sum * sum / (double) count) / ((double)count - 1));
			return denom == 0 ? Double.NaN : (v.get(v.size()-1) - sum / (double) count) / denom;
					
		}
		
	}
	
	public static Statistic Change = v -> v.get(v.size()-1) - v.get(0);
	public static Statistic PercentChange = v -> {
		return v.get(v.size()-1) / v.get(0) - 1;
	};
	public static Statistic First = v -> v.get(0);
	public static Statistic Last = v -> v.get(v.size()-1);
	
	public static class EWMA implements Statistic {
		
		double alpha = Double.NaN, value = Double.NaN;
		
		public EWMA(Optional<Double> alpha) {
			if(alpha.isPresent()) {
				this.alpha = alpha.get();
			}
		}

		@Override
		public double update(View v) {
			Array additions = v.additions();
			if(alpha != alpha) {
				alpha = 2 / (v.additions().size() + 1d);
			}
			for(int i = 0; i < additions.size(); i++) {
				double d = additions.get(i);
				if(d == d) {
					if(value != value) {
						value = d;
					} else {
						value = d * alpha + value * (1-alpha);
					}
				} else {
					if(i == additions.size()-1) {
						return Double.NaN;
					}
				}

			}
			return value;
		}
		
	}
	
	public static abstract class RankingStatistic implements Statistic {
		
		protected AVLTree t = new AVLTree();
		
		protected boolean clearOnNan;
		
		protected RankingStatistic(boolean clearOnNan) {
			this.clearOnNan = clearOnNan;
		}
		
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
						t.delete(d);
					} else if(clearOnNan) {
						t.clear();
					}
				}
			}
			Array additions = v.additions();
			for(int i = 0; i < additions.size(); i++) {
				double d = additions.get(i);
				if(d == d) {
					t.insert(d);
				}
			}
			return get();
		}
		
	}
	
	public static class Max extends RankingStatistic {
		
		public Max(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		protected double get() {
			return t.isEmpty() ? Double.NaN : t.getMax();
		}
	}

	public static class Min extends RankingStatistic {

		public Min(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		protected double get() {
			return t.isEmpty() ? Double.NaN : t.getMin();
		}
		
	}

	public static class Median extends RankingStatistic {

		public Median(boolean clearOnNan) {
			super(clearOnNan);
		}

		@Override
		protected double get() {
			return t.isEmpty() ? Double.NaN : t.getMedian();
		}
	}
}