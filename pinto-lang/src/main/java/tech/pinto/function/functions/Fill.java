package tech.pinto.function.functions;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.DoubleStream;

import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.Column;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.function.ComposableFunction;

public class Fill extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("freq", "BQ-DEC", "Period of time to look back.");
	public static final FunctionHelp.Builder FILL_HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Fills missing data with last good obseration, looking back one *freq* to fill first.");
	public static final FunctionHelp.Builder LOOKBACK_HELP_BUILDER = new FunctionHelp.Builder()
			.description("Fills missing data with last good obseration.");

	private final boolean lookBack;

	public Fill(String name, ComposableFunction previousFunction, Indexer indexer, boolean lookBack) {
		super(name, previousFunction, indexer);
		this.lookBack = lookBack;
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void apply(LinkedList<Column> stack) {
		@SuppressWarnings("rawtypes")
		Periodicity p = Periodicities.get(parameters.get().getArgument("freq"));
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		for (Column function : inputStack) {
			stack.add(new Column(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				DoubleStream input = null;
				int skip = 0;
				if (!lookBack) {
					input = inputs[0].getCells(range);
				} else {
					Period start = p.previous(p.from(range.start().endDate()));
					PeriodicRange<Period> r = (PeriodicRange<Period>) range.periodicity().range(start.endDate(),
							range.end().endDate(), range.clearCache());
					skip = (int) r.indexOf(range.start());
					input = inputs[0].getCells(r);
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
	}

}
