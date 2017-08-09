package tech.pinto.function.functions;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class Range extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("range", "0:5", "Range formatted as start(inclusive):stop(exclusive)");

	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Creates columns of constant integers");

	public Range(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}
	
	@Override
	protected void apply(LinkedList<Column> stack) {
		int start = 0, stop = 5;
		String range = parameters.get().getArgument("range");
		if(range.startsWith(":") || ! range.contains(":")) {
			stop = Integer.parseInt(range.replaceAll(":", ""));
		} else if(range.endsWith(":")) {
			start = Integer.parseInt(range.replaceAll(":", ""));
		} else  {
			String[] r = range.split(":");
			stop = Integer.parseInt(r[1]);
			start = Integer.parseInt(r[0]);
		}
		IntStream.range(start,stop).mapToDouble(i -> (double)i).mapToObj(
				value -> new Column(inputs -> Double.toString(value),
						inputs -> rng -> DoubleStream.iterate(value, r -> value).limit(rng.size()))).forEach(stack::addFirst);;
	}
}
