package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.function.UnaryFunction;

public class Fill extends IntermediateFunction {

	public Fill(LinkedList<Function> inputs, String... args) {
		super("fill", inputs, args);
		outputCount = inputStack.size();
	}

	
	@Override public Function getReference() {
		return new UnaryFunction(toString(),inputStack.removeFirst(), f -> range -> {
			TimeSeries input = f.evaluate(range);
			final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
			return new TimeSeries(range, joinWithSpaces(input.getLabel(), toString()),
					input.stream().map(d -> {
				if (!Double.isNaN(d)) {
					lastGoodValue.set(d);
				}
				return lastGoodValue.get();
			}));
		});
	}
	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("fill")
				.inputs("any<sub>1</sub>...any<sub>n</sub>")
				.outputs("any<sub>1</sub>...any<sub>n</sub>")
				.description("Fills missing data with last good obseration.")
				.build();
	}

}
