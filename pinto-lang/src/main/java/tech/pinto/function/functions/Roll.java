package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;

public class Roll extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("times", "1", "Number of times to permute");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("inputs.");
	
	public Roll(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		int times = Integer.parseInt(parameters.get().getArgument("times"));
		for(int i = 0; i < times; i++) {
			stack.addFirst(stack.removeLast());
		}
	}
}
