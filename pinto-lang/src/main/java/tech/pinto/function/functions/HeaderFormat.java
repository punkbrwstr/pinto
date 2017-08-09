package tech.pinto.function.functions;

import java.text.MessageFormat;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;

public class HeaderFormat extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("format", true, "Format string (\"{}\" is existing header)");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Formats headers according to supplied format string.");

	public HeaderFormat(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	protected String getHeaderFormat(LinkedList<Column> stack) {
		String format = parameters.get().getArgument("format");
		format = format.replaceAll("\\{\\}", "\\{0\\}");
		return format;
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		MessageFormat mf = new MessageFormat(getHeaderFormat(stack));
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		for (Column old : inputStack) {
			stack.addFirst(new Column(i -> mf.format(new Object[] { old.toString() }), old.getSeriesFunction(),
					old.getInputs()));
		}
	}
}
