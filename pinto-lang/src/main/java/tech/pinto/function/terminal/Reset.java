package tech.pinto.function.terminal;

import java.util.LinkedList;
import java.util.function.Supplier;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;

public class Reset extends TerminalFunction {


	public Reset(LinkedList<Function> inputs, String[] args) {
		super("reset", inputs, args);
	}
	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("reset")
				.description("Clears the stack.")
				.build();
	}

}
