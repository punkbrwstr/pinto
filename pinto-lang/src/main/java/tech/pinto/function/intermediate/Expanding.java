package tech.pinto.function.intermediate;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import tech.pinto.Indexer;
import tech.pinto.Column;
import tech.pinto.ColumnValues;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

public class Expanding extends ComposableFunction {

	private final Supplier<DoubleCollector> collectorSupplier;
	private final Optional<LocalDate> start;
	private final Optional<Periodicity<?>> windowFrequency;

	public Expanding(String name, ComposableFunction previousFunction, Indexer indexer,
			Supplier<DoubleCollector> collectorSupplier, String... args) {
		super(name, previousFunction, indexer, args);
		this.collectorSupplier = collectorSupplier;
		start = args.length == 0 ? Optional.empty() : Optional.of(LocalDate.parse(args[0]));
		if (args.length < 2) {
			windowFrequency = Optional.empty();
		} else {
			Periodicity<?> p = Periodicities.get(args[1].replaceAll("\\s+", ""));
			if (p == null) {
				throw new IllegalArgumentException("invalid periodicity code for window: \"" + args[1] + "\"");
			}
			windowFrequency = Optional.of(p);
		}
	}

	public static FunctionHelp getHelp(String name, String description) {
		return new FunctionHelp.Builder(name).outputs("n")
				.description("Calculates " + description
						+ " over an expanding window starting *start_date* over *periodicity*.")
				.parameter("start_date", "1", null).parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}").build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public LinkedList<Column> composeIndexed(LinkedList<Column> stack) {
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
				ColumnValues input = null;
				DoubleCollector dc = collectorSupplier.get();
				input = (ColumnValues) inputs[0].getValues(window);
				double[] output = input.getSeries().map(d -> {
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
