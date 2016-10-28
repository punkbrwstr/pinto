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
		ComposableFunction f = new ComposableFunction(args[0], previousFunction.orElseThrow(() ->
					new PintoSyntaxException("Cannot define an empty expression.")), indexer, args);
		previousFunction.get().setIsSubFunction();
		String desc = previousFunction.get().toExpression().toString();
		if(args.length > 1) {
			desc += " (" + args[1] + ")";
		}
		namespace.define(args[0], desc, f);
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
