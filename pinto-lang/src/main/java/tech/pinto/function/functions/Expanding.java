package tech.pinto.function.functions;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

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

public class Expanding extends ComposableFunction {

	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("start", false, "Start date for expanding window (yyyy-mm-dd)")
			.add("freq", "B", "Periodicity of window {B,W-FRI,BM,BQ-DEC,BA}");

	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Copies inputs.");

	private final Supplier<DoubleCollector> collectorSupplier;

	public Expanding(String name, ComposableFunction previousFunction, Indexer indexer,
			Supplier<DoubleCollector> collectorSupplier) {
		super(name, previousFunction, indexer);
		this.collectorSupplier = collectorSupplier;
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void apply(LinkedList<Column> stack) {
		Optional<LocalDate> start = parameters.get().getArgument("start") == null ? Optional.empty() :
			Optional.of(LocalDate.parse(parameters.get().getArgument("start")));
		Optional<Periodicity<?>> windowFrequency = 
			Optional.of(Periodicities.get(parameters.get().getArgument("freq")));
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		for (Column function : inputStack) {
			stack.add(new Column(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				Periodicity<Period> wf = (Periodicity<Period>) windowFrequency.orElse(range.periodicity());
				LocalDate startDate = start.orElse(range.start().endDate());
				Period windowStart = wf.from(startDate);
				Period windowEnd = wf.from(range.end().endDate());
				windowEnd = windowEnd.isBefore(windowStart) ? windowStart : windowEnd;
				PeriodicRange<Period> window = wf.range(windowStart, windowEnd, range.clearCache());

				DoubleStream.Builder b = DoubleStream.builder();
				DoubleCollector dc = collectorSupplier.get();
				double[] output = inputs[0].getCells(window).map(d -> {
						dc.add(d);
						return dc.finish();
					}).toArray();
				for (Period p : range.values()) {
					int index = (int) window.indexOf(p.endDate());
					if (index >= 0) {
						b.accept(output[index]);
					} else {
						b.accept(Double.NaN);
					}
				}
				return b.build();
			}, function));
		}
	}

}
