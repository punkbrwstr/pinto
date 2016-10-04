package tech.pinto.function.intermediate;

import java.util.function.ToDoubleFunction;

public class DoubleCollector {
		
		private double sumOfSquares = 0, sum = 0, mean = 0, sumOfLogs = 0, first = Double.NaN,
				last = Double.NaN, min = Double.NaN, max = Double.NaN;
		private int n = 0;
		private ToDoubleFunction<DoubleCollector> function;
		
		public DoubleCollector(ToDoubleFunction<DoubleCollector> function) {
			this.function = function;
		}
		
		public void add(double d) {
			last = d;
			if(!Double.isNaN(d)) {
				if(Double.isNaN(first)) {
					first = d;
				}
				if(Double.isNaN(min) || d < min) {
					min = d;
				}
				if(Double.isNaN(max) || d > max) {
					max = d;
				}
				sum += d;
				sumOfSquares += Math.pow(d, 2);
				sumOfLogs += Math.log(d);
				n++;
				mean += (d - mean) / n;
			}
		}
		
		public void combine(DoubleCollector vc) {
			int totalN = n + vc.n;
			mean = mean * n / (double) totalN + vc.mean * vc.n / (double) totalN;
			last = vc.last;
			sum += vc.sum;
			sumOfSquares += vc.sumOfSquares;
			sumOfLogs += vc.sumOfLogs;
			n += vc.n;
			min = min < vc.min ? min : vc.min;
			max = max > vc.max ? max : vc.max;
		}
		
		public double finish() {
			return function.applyAsDouble(this);
		}

		public double getSumOfSquares() {
			return sumOfSquares;
		}

		public double getSum() {
			return sum;
		}

		public double getMean() {
			return mean;
		}

		public double getSumOfLogs() {
			return sumOfLogs;
		}

		public double getFirst() {
			return first;
		}

		public double getLast() {
			return last;
		}

		public int count() {
			return n;
		}

		public double getMin() {
			return min;
		}

		public double getMax() {
			return max;
		}

		public int getN() {
			return n;
		}
		
}
