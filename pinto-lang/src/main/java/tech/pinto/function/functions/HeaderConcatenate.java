package tech.pinto.function.functions;


import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;

public class HeaderConcatenate extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("joiner", "", "String to insert between each header");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Concatenates input headers into one header literal");

	public HeaderConcatenate(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		String s = inputStack.stream().map(Column::getHeader).collect(Collectors.joining(parameters.get().getArgument("joiner")));
		stack.add(new Column(s));
	}
}
