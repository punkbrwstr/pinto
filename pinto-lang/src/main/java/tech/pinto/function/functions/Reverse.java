package tech.pinto.function.functions;

import java.util.Collections;
import java.util.LinkedList;


import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Reverse extends ComposableFunction {

	
	public Reverse(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
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
