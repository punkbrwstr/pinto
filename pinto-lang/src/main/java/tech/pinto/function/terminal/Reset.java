package tech.pinto.function.terminal;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;

public class Reset extends TerminalFunction {


	public Reset(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, namespace, previousFunction, indexer, args);
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Clears the stack.")
				.build();
	}

}
