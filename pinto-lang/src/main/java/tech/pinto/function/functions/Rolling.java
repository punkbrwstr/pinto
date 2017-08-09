package tech.pinto.function.functions;

import java.util.Arrays;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.Column;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.DoubleCollector;


public class Rolling extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("size", "1", "Size of window")
			.add("freq", false, "Periodicity of window {B,W-FRI,BM,BQ,BA}");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Calculates {0} over rolling window")
			.parameters(PARAMETERS_BUILDER.build());
	

	private final Supplier<DoubleCollector> collectorSupplier;
	private final boolean countIncludesCurrent;
	
	public Rolling(String name, ComposableFunction previousFunction, Indexer indexer,
			Supplier<DoubleCollector> collectorSupplier, boolean countIncludesCurrent) {
		super(name, previousFunction, indexer);
		this.collectorSupplier = collectorSupplier;
		this.countIncludesCurrent = countIncludesCurrent;
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void apply(LinkedList<Column> stack) {
		int size = (countIncludesCurrent ? 0 : 1) +
				Integer.parseInt(parameters.get().getArgument("size"));
		Optional<Periodicity<?>> windowFrequency = !parameters.get().hasArgument("freq") ? Optional.empty() :
			Optional.of(Periodicities.get(parameters.get().getArgument("freq")));
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		for (Column col : inputStack) {
			stack.add(new Column(inputs -> join(inputs[0].toString(), parameters.get().toString(),toString()), inputs -> range -> {
				Periodicity<Period> wf = (Periodicity<Period>) windowFrequency.orElse(range.periodicity());
				Period expandedWindowStart = wf.offset(wf.from(range.start().endDate()), -1 * (size - 1));
				Period windowEnd = wf.from(range.end().endDate());
				PeriodicRange<Period> expandedWindow = wf.range(expandedWindowStart, windowEnd, range.clearCache());
				Builder b = DoubleStream.builder();
				double[] data = inputs[0].getCells(expandedWindow).toArray();
				for(Period p : range.values()) {
					long windowStartIndex = wf.distance(expandedWindowStart, wf.from(p.endDate())) - size + 1;
					DoubleCollector dc = Arrays.stream(data, (int) windowStartIndex, (int) windowStartIndex + size)
								.collect(collectorSupplier, (v,d) -> v.add(d), (v,v1) -> v.combine(v1));
					b.accept(dc.finish());
				}
				return b.build();
			}, col));
		}
	
	}
}
