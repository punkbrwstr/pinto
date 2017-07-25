package tech.pinto.function.functions;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import tech.pinto.Indexer;
import tech.pinto.Column;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.DoubleCollector;

public class Expanding extends ComposableFunction {

	private final Supplier<DoubleCollector> collectorSupplier;

	public Expanding(String name, ComposableFunction previousFunction, Indexer indexer,
			Supplier<DoubleCollector> collectorSupplier) {
		super(name, previousFunction, indexer);
		this.collectorSupplier = collectorSupplier;
	}

	public static FunctionHelp getHelp(String name, String description) {
		return new FunctionHelp.Builder(name).outputs("n")
				.description("Calculates " + description
						+ " over an expanding window starting *start_date* over *periodicity*.")
				.parameter("start_date", "1", null).parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}").build();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		Optional<Periodicity<?>> windowFrequency;
		Optional<LocalDate> start = getArgs().length == 0 ? Optional.empty() : Optional.of(LocalDate.parse(getArgs()[0]));
		if (getArgs().length < 2) {
			windowFrequency = Optional.empty();
		} else {
			Periodicity<?> p = Periodicities.get(getArgs()[1].replaceAll("\\s+", ""));
			if (p == null) {
				throw new IllegalArgumentException("invalid periodicity code for window: \"" + getArgs()[1] + "\"");
			}
			windowFrequency = Optional.of(p);
		}
		LinkedList<Column> outputs = new LinkedList<>();
		for (Column function : stack) {
			outputs.add(new Column(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				Periodicity<Period> wf = (Periodicity<Period>) windowFrequency.orElse(range.periodicity());
				LocalDate startDate = start.orElse(range.start().endDate());
				Period windowStart = wf.from(startDate);
				Period windowEnd = wf.from(range.end().endDate());
				windowEnd = windowEnd.isBefore(windowStart) ? windowStart : windowEnd;
				PeriodicRange<Period> window = wf.range(windowStart, windowEnd, range.clearCache());

				DoubleStream.Builder b = DoubleStream.builder();
				DoubleCollector dc = collectorSupplier.get();
				double[] output = inputs[0].getSeries(window).get().map(d -> {
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
		return outputs;
	}

}
