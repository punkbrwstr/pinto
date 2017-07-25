package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

final public class Clear extends ComposableFunction {
	
	

	public Clear(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		stack.clear();
		return stack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Removes inputs from stack")
				.outputs("none")
				.build();
	}
	
}
