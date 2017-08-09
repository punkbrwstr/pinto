package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

final public class Clear extends ComposableFunction {
	
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Removes inputs from the stack.");
	

	public Clear(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		stack.clear();
	}
}
