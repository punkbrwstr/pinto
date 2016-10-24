package tech.pinto.function.intermediate;

import java.util.LinkedList;


import tech.pinto.function.Function;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ReferenceFunction;

public class Comment extends ReferenceFunction {

	public Comment(String name, LinkedList<Function> inputs) {
		super(name, inputs, new String[]{});
		// does nothing
	}

	@Override
	public Function getReference() {
		return inputStack.removeLast();
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("0")
				.description("Just a comment.")
				.parameter("Comment text",null, null)
				.build();
	}

	@Override
	public int getOutputCount() {
		return inputStack.size();
	}
}
