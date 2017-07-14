package tech.pinto.function.functions;

import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class HeaderPrepend extends HeaderFormat {

	public HeaderPrepend(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	protected String getHeaderFormat() {
		return getArgs()[0] + "{0}";
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Prepends argument string to headers.")
				.parameter("string to prepend")
				.build();
	}
}
