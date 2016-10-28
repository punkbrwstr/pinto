package tech.pinto.function.intermediate;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

final public class Clear extends ComposableFunction {
	
	

	public Clear(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
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
