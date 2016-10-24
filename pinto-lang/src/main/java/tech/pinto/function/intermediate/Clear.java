package tech.pinto.function.intermediate;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.ReferenceFunction;

final public class Clear extends ReferenceFunction {
	
	public Clear(String name, LinkedList<Function> inputs, String...args) {
		super(name, inputs, args);
	}
	
	@Override public Function getReference() {
		throw new UnsupportedOperationException();
	}


	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Removes inputs from stack")
				.outputs("none")
				.build();
	}

	@Override
	public int getOutputCount() {
		return 0;
	}

	
}
