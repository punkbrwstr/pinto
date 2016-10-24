package tech.pinto.function.terminal;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;

public class Reset extends TerminalFunction {


	public Reset(String name, LinkedList<Function> inputs, String[] args) {
		super(name, inputs, args);
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Clears the stack.")
				.build();
	}

}
