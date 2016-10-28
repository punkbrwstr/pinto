package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import tech.pinto.Indexer;
import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.function.ComposableFunction;


public class Cross extends ComposableFunction {
	
	private final Supplier<DoubleCollector> collectorSupplier;
	
	
	public Cross(String name, ComposableFunction previousFunction, Indexer indexer, Supplier<DoubleCollector> collectorSupplier, String... args) {
		super(name, previousFunction, indexer, args);
		this.collectorSupplier = collectorSupplier;
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		return asList(new EvaluableFunction(inputs -> toString(),
				inputs -> range -> {
					Builder b = DoubleStream.builder();
					List<OfDouble> l = stack.stream().map(c -> c.evaluate(range)).map(c -> (TimeSeries) c)
								.map(TimeSeries::stream).map(ds -> ds.iterator()).collect(Collectors.toList());
					for(int i = 0; i < range.size(); i++) {
						DoubleCollector dc = collectorSupplier.get();
						l.forEach(di -> dc.add(di.nextDouble()));
						b.accept(dc.finish());
					}
					return b.build();
				}, stack.toArray(new EvaluableFunction[]{})));
	}

	public static FunctionHelp getHelp(String name, String description) {
		return new FunctionHelp.Builder(name)
				.outputs("1")
				.description("Calculates " + description + " across inputs.")
				.build();
	}

}
