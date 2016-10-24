package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.concurrent.atomic.AtomicReference;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.ReferenceFunction;
import tech.pinto.function.LambdaFunction;

public class Fill extends ReferenceFunction {

	public Fill(String name, LinkedList<Function> inputs, String... args) {
		super(name, inputs, args);
	}

	
	@Override public Function getReference() {
		final Function function = inputStack.removeFirst();
		return new LambdaFunction(f -> join(f.getStack().getFirst().toString(),toString()),
			f -> range -> {
				TimeSeries input = f.removeFirst().evaluate(range);
				final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
				return input.stream().map(d -> {
					if (!Double.isNaN(d)) {
						lastGoodValue.set(d);
					}
					return lastGoodValue.get();
				});
		}, function);
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("*n*")
				.description("Fills missing data with last good obseration.")
				.build();
	}


	@Override
	public int getOutputCount() {
		return inputStack.size();
	}

}
