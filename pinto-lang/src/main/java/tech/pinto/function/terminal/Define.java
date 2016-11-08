package tech.pinto.function.terminal;


import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.TerminalFunction;

public class Define extends TerminalFunction {

	public Define(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, namespace, previousFunction, indexer, args);
	}
	
	public Optional<String> getText() throws PintoSyntaxException {
		if(args.length < 1) {
			throw new IllegalArgumentException("Define requires a name argument.");
		}
		String desc = previousFunction.get().toExpression().toString();
		previousFunction.get().setIsSubFunction();
		if(args.length > 1) {
			desc += " (" + args[1] + ")";
		}
		if(args.length > 2 && Boolean.parseBoolean(args[2].trim())) {
	        previousFunction.get().getHead().setIndexer(Indexer.NONE);
		}
		namespace.define(args[0], desc, previousFunction.get());
		return Optional.of("Successfully saved.");
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("none")
				.description("Defines the preceding function as *name*.")
				.parameter("name")
				.parameter("description (Optional)")
				.build();
	}

}
