package tech.pinto.function.intermediate;



import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.ReferenceFunction;

public class Roll extends ReferenceFunction {
	
	private final int times;
	
	public Roll(String name, LinkedList<Function> inputs, String[] args) {
		super(name, inputs, args);
		times = args.length < 2 ? 1 : Integer.parseInt(args[1]);
		for(int i = 0; i < times; i++) {
			inputStack.addFirst(inputStack.removeLast());
		}
	}
	
	@Override public Function getReference() {
		return inputStack.removeFirst();
	}


	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("*n*")
				.description("Permutes input stack elements *m* times")
				.parameter("m","2",null)
				.build();
	}

	@Override
	public int getOutputCount() {
		return inputStack.size();
	}
	

}
