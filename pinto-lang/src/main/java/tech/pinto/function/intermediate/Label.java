package tech.pinto.function.intermediate;

import java.util.ArrayDeque;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Supplier;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.function.UnaryFunction;

public class Label extends IntermediateFunction {
	
	private final ArrayDeque<String> labels = new ArrayDeque<>();

	public Label(LinkedList<Function> inputs, String... arguments) {
		super("label", inputs, arguments);
		outputCount = inputStack.size();
		labels.addAll(Arrays.asList(arguments));
	}
	
	@Override public Function getReference() {
		String label = labels.removeFirst();
		return new UnaryFunction("label",inputStack.removeFirst(), 
			f -> range -> {
				TimeSeries data = f.evaluate(range);
				data.setLabel(label);
				return data;
		});
	}

	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("label")
				.outputs("*z*")
				.description("Sets arguments as labels for inputs")
				.parameter("label<sub>1</sub>")
				.parameter("label<sub>z</sub>")
				.build();
	}
	

}
