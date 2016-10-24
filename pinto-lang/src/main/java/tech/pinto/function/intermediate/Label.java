package tech.pinto.function.intermediate;

import java.util.ArrayDeque;


import java.util.Arrays;
import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.ReferenceFunction;

public class Label extends ReferenceFunction {
	
	private final ArrayDeque<String> labels = new ArrayDeque<>();

	public Label(String name, LinkedList<Function> inputs, String... arguments) {
		super(name, inputs, arguments);
		labels.addAll(Arrays.asList(arguments));
	}
	
	@Override public Function getReference() {
		String label = labels.removeFirst();
		Function function = inputStack.removeFirst();
		function.setLabeller(f -> label);
		return function;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("*z*")
				.description("Sets arguments as labels for inputs")
				.parameter("label<sub>1</sub>")
				.parameter("label<sub>z</sub>")
				.build();
	}

	@Override
	public int getOutputCount() {
		return inputStack.size();
	}
	

}
