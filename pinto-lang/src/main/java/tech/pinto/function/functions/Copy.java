package tech.pinto.function.functions;

import java.util.ArrayDeque;


import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;

public class Copy extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("times", "2", "Number of copies of stack inputs to make.");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Copies inputs.");
	
	
	public Copy(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		int times = Integer.parseInt(parameters.get().getArgument("times"));
		ArrayDeque<Column> temp = new ArrayDeque<>();
        stack.stream().forEach(temp::addFirst);
        for(int i = 0; i < times - 1; i++) {
        	temp.stream().map(Column::clone).forEach(stack::addFirst);
        }
	}
	
}
