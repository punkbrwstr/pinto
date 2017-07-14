package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Roll extends ComposableFunction {
	
	public Roll(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		int times = getArgs().length == 0 ? 1 : Integer.parseInt(getArgs()[0]);
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
