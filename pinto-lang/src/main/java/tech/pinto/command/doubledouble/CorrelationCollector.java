package tech.pinto.command.doubledouble;

public class CorrelationCollector {
		
		private double[][] covar;
		private double[][] correl;
		private double[] means;
		
		private int n = 0, inputCount;
		
		public CorrelationCollector(int count) {
			correl = new double[count][count];
			covar = new double[count][count];
			means = new double[count];
			this.inputCount = count;
		}
		
		public void add(double[] d) {
			n++;
			for(int i = 0; i < inputCount; i++) {
				if(!Double.isNaN(d[i])) {
					means[i] += (d[i] - means[i]) / (double) n; 
				}
			}
			if(n==1) return;
			for(int i = 0; i < inputCount; i++) {
				for(int j = 0; j <= i; j++) {
					if(!(Double.isNaN(d[i]) || Double.isNaN(d[j]))) {
						covar[i][j] = covar[i][j] * (n - 1) / (double) n 
							+ (means[i] - d[i]) * (means[j] - d[j]) * (1 / ((double) n - 1));
					}
				}
			}
			for(int i = 0; i < inputCount; i++) {
				for(int j = 0; j < i; j++) {
					correl[i][j] = covar[i][j] / Math.sqrt(covar[j][j]) / Math.sqrt(covar[i][i]);
				}
			}
		}
		
		public double getAverage() {
			double d = 0;
			for(int i = 1; i < inputCount; i++) {
				for(int j = 0; j < i; j++) {
					d += correl[i][j];
				}
			}
			return d / ((inputCount - 1) * inputCount / 2.0);
		}
}
