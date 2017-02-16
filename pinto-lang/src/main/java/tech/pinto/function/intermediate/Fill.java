package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.concurrent.atomic.AtomicReference;

import tech.pinto.Indexer;
import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.EvaluableFunction;

public class Fill extends ComposableFunction {

	private final boolean lookBack;
	private final String periodicityCode;
	
	public Fill(String name, ComposableFunction previousFunction, Indexer indexer, boolean lookBack, String... args) {
		super(name, previousFunction, indexer, args);
		this.lookBack = lookBack;
		this.periodicityCode = args.length < 1 ? "BQ-DEC" : args[0];
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Fills missing data with last good obseration.")
				.build();
	}

	public static FunctionHelp getLookbackHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Fills missing data with last good obseration, looking back one *periodicity* to fill first.")
				.parameter("periodicity","BQ-DEC","")
				.build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		LinkedList<EvaluableFunction> outputs = new LinkedList<>();
		for (EvaluableFunction function : stack) {
			outputs.add(new EvaluableFunction(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				TimeSeries input = null;
				int skip = 0;
				if(!lookBack) {
					input = inputs[0].evaluate(range);
				} else {
					Periodicity<Period> p = Periodicities.get(periodicityCode);
					Period start = p.previous(p.from(range.start().endDate()));
					PeriodicRange<Period> r = (PeriodicRange<Period>) range.periodicity().range(
							start.endDate(), range.end().endDate(), range.clearCache());
					skip = (int) r.indexOf(range.start());
					input = inputs[0].evaluate(r);
				}
				final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
				return input.stream().map(d -> {
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
