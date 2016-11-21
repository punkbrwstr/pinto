package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.concurrent.atomic.AtomicReference;

import tech.pinto.Indexer;
import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.EvaluableFunction;

public class Fill extends ComposableFunction {

	
	public Fill(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Fills missing data with last good obseration.")
				.build();
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		LinkedList<EvaluableFunction> outputs = new LinkedList<>();
		for (EvaluableFunction function : stack) {
			outputs.addFirst(new EvaluableFunction(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				TimeSeries input = inputs[0].evaluate(range);
				final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
				return input.stream().map(d -> {
					if (!Double.isNaN(d)) {
						lastGoodValue.set(d);
					}
					return lastGoodValue.get();
				});
			}, function));
		}
		return outputs;
	}

}
