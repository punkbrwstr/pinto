package tech.pinto.function.intermediate;

import java.util.LinkedList;
import java.util.function.Supplier;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;

public class Clear extends IntermediateFunction {
	
	public Clear(LinkedList<Function> inputs, String...args) {
		super("clear", inputs, args);
		outputCount = 0;
	}
	
	@Override public Function getReference() {
		throw new UnsupportedOperationException();
	}


	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("copy")
				.inputs("any<sub>1</sub>...any<sub>n</sub>")
				.outputs("any<sub>1</sub>...any<sub>n</sub>")
				.description("Copies stack inputs *m* times")
				.parameter("m","2",null)
				.build();
	}

	
}
