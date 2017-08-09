package tech.pinto.function.functions;

import java.util.Collections;
import java.util.LinkedList;


import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Reverse extends ComposableFunction {
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Reverses order of input stack");

	
	public Reverse(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		Collections.reverse(stack);
	}
}
