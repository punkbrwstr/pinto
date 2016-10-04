package tech.pinto.function.intermediate;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.function.Supplier;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;

public class Copy extends IntermediateFunction {
	
	public Copy(LinkedList<Function> inputs, String...args) {
		super("copy", inputs, args);
		int times = args.length == 0 ? 2 : Integer.parseInt(args[0]);
		outputCount = inputStack.size() * times;
		ArrayDeque<Function> temp = new ArrayDeque<>();
        inputStack.stream().forEach(temp::addFirst);
        for(int i = 0; i < times - 1; i++) {
        	temp.stream().map(Function::clone).forEach(inputStack::addFirst);
        }
	}
	
	@Override public Function getReference() {
		return inputStack.removeFirst();
	}


	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("copy")
				.description("Copies stack inputs *m* times")
				.parameter("m","2",null)
				.outputs("*n*  *  *m*")
				.build();
	}

	
}
