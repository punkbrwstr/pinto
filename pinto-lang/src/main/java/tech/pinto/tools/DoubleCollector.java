package tech.pinto.tools;


public class DoubleCollector {
	
		@FunctionalInterface
		public static interface Aggregation {
			public double apply(double[] d);
		}
		
		
		public static double first(double[] d) {
			return d[0];
		}

		public static double last(double[] d) {
			return d[d.length-1];
		}

		public static double change(double[] d) {
			return d[d.length-1] - d[0];
		}

		public static double pct_change(double[] d) {
			return d[d.length-1] / d[0] - 1;
		}

		public static double log_change(double[] d) {
			return Math.log(d[d.length-1] / d[0]);
		}

		public static double count(double[] d) {
			int count = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					count++;
				}
			}
			return (double) count;
		}

		public static double mean(double[] d) {
			int count = 0;
			double sum = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					count++;
					sum += value;
				}
			}
			return sum / (double) count;
		}

		public static double geo_mean(double[] d) {
			int count = 0;
			double sum = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					count++;
					sum += Math.log(value);
				}
			}
			return Math.exp(sum / (double) count);
		}

		public static double sum(double[] d) {
			double sum = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					sum += value;
				}
			}
			return sum;
		}

		public static double max(double[] d) {
			double max = Double.NaN;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					if(max != max || value > max) {
						max = value;
					}
				}
			}
			return max;
		}

		public static double min(double[] d) {
			double min = Double.NaN;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					if(min != min || value < min) {
						min = value;
					}
				}
			}
			return min;
		}
		
		public static double varp(double[] d) {
			int count = 0;
			double sum = 0;
			double sumOfSquares = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					count++;
					sum += value;
					sumOfSquares += Math.pow(value, 2);
				}
			}
			double mean = sum / (double) count;
			return (sumOfSquares - sum * mean) / (double) count;
		}

		public static double var(double[] d) {
			int count = 0;
			double sum = 0;
			double sumOfSquares = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					count++;
					sum += value;
					sumOfSquares += Math.pow(value, 2);
				}
			}
			double mean = sum / (double) count;
			return (sumOfSquares - sum * mean) / (double) (count - 1);
		}

		public static double std(double[] d) {
			return Math.sqrt(var(d));
		}

		public static double stdp(double[] d) {
			return Math.sqrt(varp(d));
		}

		public static double zscore(double[] d) {
			int count = 0;
			double sum = 0;
			double sumOfSquares = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					count++;
					sum += value;
					sumOfSquares += Math.pow(value, 2);
				}
			}
			double mean = sum / (double) count;
			double std = Math.sqrt((sumOfSquares - sum * mean) / (double) (count - 1));
			return (d[d.length-1] - mean) / std;
		}
		
		public static double zscorep(double[] d) {
			int count = 0;
			double sum = 0;
			double sumOfSquares = 0;
			for(int i = 0; i < d.length; i++) {
				double value = d[i];
				if(value == value) {
					count++;
					sum += value;
					sumOfSquares += Math.pow(value, 2);
				}
			}
			double mean = sum / (double) count;
			double std = Math.sqrt((sumOfSquares - sum * mean) / (double) (count));
			return (d[d.length-1] - mean) / std;
		}
}
