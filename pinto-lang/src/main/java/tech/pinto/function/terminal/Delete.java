package tech.pinto.function.terminal;

import java.util.LinkedList;


import java.util.Optional;

import tech.pinto.Namespace;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;

public class Delete extends TerminalFunction {

	public Delete(String name, Namespace namespace, LinkedList<Function> inputs, String... arguments) {
		super(name, inputs, arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("del requires one argument.");
		}
		namespace.undefine(arguments[0]);
		message = Optional.of("Successfully deleted.");
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("none")
				.description("Deletes previously defined command *name*.")
				.parameter("name")
				.build();
	}	
	

}
