package tech.pinto.function.intermediate;

import java.util.ArrayDeque;
import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.ReferenceFunction;

public class Copy extends ReferenceFunction {
	
	private final int times;
	
	public Copy(LinkedList<Function> inputs, String...args) {
		super("copy", inputs, args);
		times = args.length == 0 ? 2 : Integer.parseInt(args[0]);
		ArrayDeque<Function> temp = new ArrayDeque<>();
        inputStack.stream().forEach(temp::addFirst);
        for(int i = 0; i < times - 1; i++) {
        	temp.stream().map(Function::clone).forEach(inputStack::addFirst);
        }
	}
	
	@Override public Function getReference() {
		return inputStack.removeFirst();
	}


	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Copies stack inputs *m* times")
				.parameter("m","2",null)
				.outputs("n * m")
				.build();
	}

	@Override
	public int getOutputCount() {
		return inputStack.size() * times;
	}

	
}
