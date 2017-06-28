package tech.pinto.function.intermediate;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Roll extends ComposableFunction {
	
	public Roll(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<Column> composeIndexed(LinkedList<Column> stack) {
		int times = args.length == 0 ? 1 : Integer.parseInt(args[0]);
		for(int i = 0; i < times; i++) {
			stack.addFirst(stack.removeLast());
		}
		return stack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("*n*")
				.description("Permutes input stack elements *m* times")
				.parameter("m","1",null)
				.build();
	}
}
