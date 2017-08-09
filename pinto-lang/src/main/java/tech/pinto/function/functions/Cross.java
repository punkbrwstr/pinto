package tech.pinto.function.functions;

import java.util.LinkedList;



import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import tech.pinto.Indexer;
import tech.pinto.Column;
import tech.pinto.tools.DoubleCollector;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;


public class Cross extends ComposableFunction {
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Calculates cross-sectional {0} per period for all inputs.");
	
	private final Supplier<DoubleCollector> collectorSupplier;
	
	
	public Cross(String name, ComposableFunction previousFunction, Indexer indexer, Supplier<DoubleCollector> collectorSupplier) {
		super(name, previousFunction, indexer);
		this.collectorSupplier = collectorSupplier;
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		final LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		stack.add(new Column(inputs -> toString(),
				inputs -> range -> {
					Builder b = DoubleStream.builder();
					List<OfDouble> l = stack.stream().map(c -> c.getCells(range))
								.map(ds -> ds.iterator()).collect(Collectors.toList());
					for(int i = 0; i < range.size(); i++) {
						DoubleCollector dc = collectorSupplier.get();
						l.forEach(di -> dc.add(di.nextDouble()));
						b.accept(dc.finish());
					}
					return b.build();
				}, inputStack.toArray(new Column[]{})));
	}
}
