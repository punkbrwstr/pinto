package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.DoubleStream;

import tech.pinto.Indexer;
import tech.pinto.Column;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.function.ComposableFunction;

public class Fill extends ComposableFunction {

	private final boolean lookBack;

	public Fill(String name, ComposableFunction previousFunction, Indexer indexer, boolean lookBack) {
		super(name, previousFunction, indexer);
		this.lookBack = lookBack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name).outputs("n").description("Fills missing data with last good obseration.")
				.build();
	}

	public static FunctionHelp getLookbackHelp(String name) {
		return new FunctionHelp.Builder(name).outputs("n")
				.description(
						"Fills missing data with last good obseration, looking back one *periodicity* to fill first.")
				.parameter("periodicity", "BQ-DEC", "").build();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		String periodicityCode = getArgs().length < 1 ? "BQ-DEC" : getArgs()[0];
		LinkedList<Column> outputs = new LinkedList<>();
		for (Column function : stack) {
			outputs.add(new Column(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				DoubleStream input = null;
				int skip = 0;
				if (!lookBack) {
					input = inputs[0].getSeries(range).get();
				} else {
					Periodicity<Period> p = Periodicities.get(periodicityCode);
					Period start = p.previous(p.from(range.start().endDate()));
					PeriodicRange<Period> r = (PeriodicRange<Period>) range.periodicity().range(start.endDate(),
							range.end().endDate(), range.clearCache());
					skip = (int) r.indexOf(range.start());
					input = inputs[0].getSeries(r).get();
				}
				final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
				return input.map(d -> {
					if (!Double.isNaN(d)) {
						lastGoodValue.set(d);
					}
					return lastGoodValue.get();
				}).skip(skip);
			}, function));
		}
		return outputs;
	}

}
