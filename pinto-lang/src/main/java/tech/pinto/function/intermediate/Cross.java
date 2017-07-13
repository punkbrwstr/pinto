package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import tech.pinto.Indexer;
import tech.pinto.Column;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;


public class Cross extends ComposableFunction {
	
	private final Supplier<DoubleCollector> collectorSupplier;
	
	
	public Cross(String name, ComposableFunction previousFunction, Indexer indexer, Supplier<DoubleCollector> collectorSupplier) {
		super(name, previousFunction, indexer);
		this.collectorSupplier = collectorSupplier;
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		return asList(new Column(inputs -> toString(),
				inputs -> range -> {
					Builder b = DoubleStream.builder();
					List<OfDouble> l = stack.stream().map(c -> c.getSeries(range))
								.map(Optional::get).map(ds -> ds.iterator()).collect(Collectors.toList());
					for(int i = 0; i < range.size(); i++) {
						DoubleCollector dc = collectorSupplier.get();
						l.forEach(di -> dc.add(di.nextDouble()));
						b.accept(dc.finish());
					}
					return b.build();
				}, stack.toArray(new Column[]{})));
	}

	public static FunctionHelp getHelp(String name, String description) {
		return new FunctionHelp.Builder(name)
				.outputs("1")
				.description("Calculates " + description + " across inputs.")
				.build();
	}

}
