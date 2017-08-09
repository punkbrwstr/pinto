package tech.pinto.function.functions;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class HeaderAppend extends HeaderFormat {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("suffix", true, "Suffix to append");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Appends *suffix* header to headers");

	public HeaderAppend(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected String getHeaderFormat(LinkedList<Column> stack) {
		return "{0}" + parameters.get().getArgument("suffix");
	}
}
