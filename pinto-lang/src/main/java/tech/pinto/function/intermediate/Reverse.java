package tech.pinto.function.intermediate;

import java.util.LinkedList;


import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.ReferenceFunction;

public class Reverse extends ReferenceFunction {

	public Reverse(String name, LinkedList<Function> inputs, String...args) {
		super(name, inputs, args);
	}
	
	@Override public Function getReference() {
		return inputStack.removeFirst();
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("*n*")
				.description("Reverses order of inputs")
				.build();
	}

	@Override
	public int getOutputCount() {
		return inputStack.size();
	}
}
