package tech.pinto.function.functions;


import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;

public class Label extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("labels", true, "Comma delimited headers to apply to columns");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Sets the headers of columns");
	
	
	public Label(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		List<String> l = Arrays.asList(parameters.get().getArgument("labels").split(","));
		List<String> labels = l.subList(0, Math.min(l.size(), stack.size()));
		ArrayDeque<Column> temp = new ArrayDeque<>();
		for(int i = 0; i < labels.size(); i++) {
			final int index = labels.size() - i - 1;
			Column old = stack.removeFirst();
			temp.addFirst(new Column(inputs -> labels.get(index), old.getSeriesFunction(), old.getInputs()));
		}
       	temp.stream().forEach(stack::addFirst);
	}
}
