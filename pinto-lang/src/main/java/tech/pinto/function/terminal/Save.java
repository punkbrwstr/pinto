package tech.pinto.function.terminal;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


import tech.pinto.Cache;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.TerminalFunction;

public class Save extends TerminalFunction {

	public Save(Cache cache, List<String> saveString, String[] arguments) {
		super("def", new LinkedList<>(), arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("save requires one argument.");
		}
		cache.save(arguments[0], joinWithSpaces(saveString));
		message = Optional.of("Successfully saved.");
	}
	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("def")
				.outputs("none")
				.description("Defines the preceding commands as a new command, named *name*.")
				.parameter("name")
				.build();
	}
	
	
	

}
