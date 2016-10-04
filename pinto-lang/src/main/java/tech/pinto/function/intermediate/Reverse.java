package tech.pinto.function.intermediate;

import java.util.LinkedList;
import java.util.function.Supplier;


import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;

public class Reverse extends IntermediateFunction {

	public Reverse(LinkedList<Function> inputs, String...args) {
		super("rev", inputs, args);
		outputCount = inputStack.size();
	}
	
	@Override public Function getReference() {
		return inputStack.removeLast();
	}

	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("rev")
				.inputs("any<sub>1</sub>...any<sub>n</sub>")
				.outputs("any<sub>n</sub>...any<sub>1</sub>")
				.description("Reverses order of *n* stack elements")
				.parameter("n","all",null)
				.build();
	}
}
