package tech.pinto.function.terminal;

import java.util.LinkedList;

import java.util.List;
import java.util.Optional;


import tech.pinto.Namespace;
import tech.pinto.function.Function;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.intermediate.Copy;

public class Define extends TerminalFunction {

	public Define(String name, Namespace namespace, LinkedList<Function> inputs, List<String> saveString, String[] arguments) {
		super(name, new LinkedList<>(), arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("save requires one argument.");
		}
		Function f = new Copy(inputs,"1");
		f.setLabeller(func -> arguments[0]);
		String desc = join(saveString);
		if(arguments.length > 1) {
			desc += " (" + arguments[1] + ")";
		}
		namespace.define(arguments[0], desc, f);
		message = Optional.of("Successfully saved.");
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("none")
				.description("Defines the preceding commands as a new command, named *name*.")
				.parameter("name")
				.parameter("description (Optional)")
				.build();
	}
	
	
	

}
