package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;


public class Cross extends Function {
	
	private final Supplier<DoubleCollector> collectorSupplier;
	
	public Cross(String name, LinkedList<Function> inputs, 
			Supplier<DoubleCollector> collectorSupplier, String... args) {
		super(name, inputs, args);
		this.collectorSupplier = collectorSupplier;
	}
	
	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		Builder b = DoubleStream.builder();
		List<OfDouble> l = inputStack.stream().map(c -> c.evaluate(range)).map(c -> (TimeSeries) c)
					.map(TimeSeries::stream).map(ds -> ds.iterator()).collect(Collectors.toList());
		for(int i = 0; i < range.size(); i++) {
			DoubleCollector dc = collectorSupplier.get();
			l.forEach(di -> dc.add(di.nextDouble()));
			b.accept(dc.finish());
		}
		return new TimeSeries(range, toString(), b.build());
	}

	@Override
	public Function getReference() {
		return this;
	}
	
	public static FunctionHelp getHelp(String name, String description) {
		return new FunctionHelp.Builder(name)
				.outputs("1")
				.description("Calculates " + description + " across inputs.")
				.build();
	}

	@Override
	public int getOutputCount() {
		return 1;
	}



}
