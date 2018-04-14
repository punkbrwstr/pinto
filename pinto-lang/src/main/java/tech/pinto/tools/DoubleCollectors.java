package tech.pinto.tools;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public enum DoubleCollectors implements Supplier<DoubleCollector>{
	first(dc -> dc.getFirst()),
	last(dc -> dc.getLast()),
	change(dc -> dc.getLast() - dc.getFirst()),
	pct_change(dc -> dc.getLast() / dc.getFirst() - 1.0d),
	log_change(dc -> Math.log(dc.getLast()) - Math.log(dc.getFirst())),
	
	/* "true" window ones */
	mean(dc -> dc.getSum() / (double) dc.count()),
	geomean(dc -> Math.exp(dc.getSumOfLogs() / (double) dc.count())),
	max(dc -> dc.getMax()),
	min(dc -> dc.getMin()),
	sum(dc -> dc.getSum()),
	varp(dc -> (dc.getSumOfSquares() - dc.getSum() * dc.getMean()) / (double) dc.count()),
	var(dc -> (dc.getSumOfSquares() - dc.getSum() * dc.getMean()) / (double) (dc.count() - 1)),
	std(dc -> Math.sqrt(var.getFunction().applyAsDouble(dc))),
	stdp(dc -> Math.sqrt(varp.getFunction().applyAsDouble(dc))),
	zscore(dc -> (dc.getLast() - dc.getMean()) / Math.sqrt((dc.getSumOfSquares() - dc.getSum() * dc.getMean()) / (double) (dc.count() - 1))),
	zscorep(dc -> (dc.getLast() - dc.getMean()) / Math.sqrt((dc.getSumOfSquares() - dc.getSum() * dc.getMean()) / (double) dc.count())),
	;
	
	private ToDoubleFunction<DoubleCollector> function;
	
	private DoubleCollectors(ToDoubleFunction<DoubleCollector> function) {
		this.function = function;
	}
	
	public ToDoubleFunction<DoubleCollector> getFunction() {
		return function;
	}
	
	@Override
	public DoubleCollector get() {
		return new DoubleCollector(function);
	}
	
	
}
