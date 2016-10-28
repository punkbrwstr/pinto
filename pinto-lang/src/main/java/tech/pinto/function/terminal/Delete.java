package tech.pinto.function.terminal;

import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;

public class Delete extends TerminalFunction {


	public Delete(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, namespace, previousFunction, indexer, args);
	}

	public Optional<String> getText() throws PintoSyntaxException {
		if(args.length < 1) {
			throw new IllegalArgumentException("del requires one argument.");
		}
		namespace.undefine(args[0]);
		return Optional.of("Successfully deleted.");
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("none")
				.description("Deletes previously defined command *name*.")
				.parameter("name")
				.build();
	}	
}
