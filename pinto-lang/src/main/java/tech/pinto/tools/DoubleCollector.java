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
			return d[d.length-1] / d[0];
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
				if(!Double.isNaN(d[i])) {
					count++;
				}
			}
			return (double) count;
		}

		public static double mean(double[] d) {
			int count = 0;
			double sum = 0;
			for(int i = 0; i < d.length; i++) {
				if(!Double.isNaN(d[i])) {
					count++;
					sum += d[i];
				}
			}
			return sum / (double) count;
		}

		public static double geo_mean(double[] d) {
			int count = 0;
			double sum = 0;
			for(int i = 0; i < d.length; i++) {
				if(!Double.isNaN(d[i])) {
					count++;
					sum += Math.log(d[i]);
				}
			}
			return Math.exp(sum / (double) count);
		}

		public static double sum(double[] d) {
			double sum = 0;
			for(int i = 0; i < d.length; i++) {
				if(!Double.isNaN(d[i])) {
					sum += d[i];
				}
			}
			return sum;
		}

		public static double max(double[] d) {
			double max = 0;
			for(int i = 0; i < d.length; i++) {
				if(!Double.isNaN(d[i])) {
					if(Double.isNaN(max) || d[i] > max) {
						max = d[i];
					}
				}
			}
			return max;
		}

		public static double min(double[] d) {
			double min = 0;
			for(int i = 0; i < d.length; i++) {
				if(!Double.isNaN(d[i])) {
					if(Double.isNaN(min) || d[i] < min) {
						min = d[i];
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
				if(!Double.isNaN(d[i])) {
					count++;
					sum += d[i];
					sumOfSquares += Math.pow(d[i], 2);
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
				if(!Double.isNaN(d[i])) {
					count++;
					sum += d[i];
					sumOfSquares += Math.pow(d[i], 2);
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
				if(!Double.isNaN(d[i])) {
					count++;
					sum += d[i];
					sumOfSquares += Math.pow(d[i], 2);
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
				if(!Double.isNaN(d[i])) {
					count++;
					sum += d[i];
					sumOfSquares += Math.pow(d[i], 2);
				}
			}
			double mean = sum / (double) count;
			double std = Math.sqrt((sumOfSquares - sum * mean) / (double) (count));
			return (d[d.length-1] - mean) / std;
		}
}
