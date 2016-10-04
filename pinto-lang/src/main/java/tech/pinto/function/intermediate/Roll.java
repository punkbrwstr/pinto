package tech.pinto.function.intermediate;



import java.util.LinkedList;
import java.util.function.Supplier;


import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;

public class Roll extends IntermediateFunction {
	
	private final int times;
	
	public Roll(LinkedList<Function> inputs, String[] args) {
		super("roll", inputs, args);
		times = args.length < 2 ? 1 : Integer.parseInt(args[1]);
		outputCount = inputStack.size();
		for(int i = 0; i < times; i++) {
			inputStack.addFirst(inputStack.removeLast());
		}
	}
	
	@Override public Function getReference() {
		return inputStack.removeFirst();
	}


	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("roll")
				.outputs("*n*")
				.description("Permutes input stack elements *m* times")
				.parameter("m","2",null)
				.build();
	}
	

}
