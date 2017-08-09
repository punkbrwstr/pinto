package tech.pinto.function.functions;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class HeaderPrepend extends HeaderFormat {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("prefix", true, "Header to prepend to others");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Prepends *prefix* to headers");
	
	public HeaderPrepend(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected String getHeaderFormat(LinkedList<Column> stack) {
		return parameters.get().getArgument("prefix") + "{0}";
	}
}
