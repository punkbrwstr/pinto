package tech.pinto.function.header;

import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class HeaderAppend extends HeaderFormat {

	public HeaderAppend(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	protected String getHeaderFormat() {
		return "{0}" + getArgs()[0];
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Appends argument string to headers.")
				.parameter("string to append")
				.build();
	}
}
