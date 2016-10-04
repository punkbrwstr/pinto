package tech.pinto.function.terminal;

import java.util.LinkedList;

import java.util.Optional;
import java.util.function.Supplier;

import tech.pinto.Cache;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;

public class Delete extends TerminalFunction {

	public Delete(Cache cache, LinkedList<Function> inputs, String... arguments) {
		super("del", inputs, arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("del requires one argument.");
		}
		cache.deleteSaved(arguments[0]);
		message = Optional.of("Successfully deleted.");
	}
	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("del")
				.outputs("none")
				.description("Deletes previously defined command *name*.")
				.parameter("name")
				.build();
	}	
	

}
