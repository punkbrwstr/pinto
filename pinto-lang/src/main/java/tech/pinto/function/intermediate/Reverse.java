package tech.pinto.function.intermediate;

import java.util.Collections;
import java.util.LinkedList;


import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Reverse extends ComposableFunction {

	
	public Reverse(String name, ComposableFunction previousFunction, Indexer indexer, String[] args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		Collections.reverse(stack);
		return stack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("*n*")
				.description("Reverses order of inputs")
				.build();
	}

}
