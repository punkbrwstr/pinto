package tech.pinto.function.intermediate;


import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Label extends ComposableFunction {
	
	
	public Label(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		int stackIndex = 0;
		int i = -1 +  Math.min(args.length, stack.size());
		while(i >= 0) {
			final int labelIndex = i--;
			stack.get(stackIndex++).setLabeller(inputs -> args[labelIndex]);
		}
		return stack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("*z*")
				.description("Sets arguments as labels for inputs")
				.parameter("label<sub>1</sub>")
				.parameter("label<sub>z</sub>")
				.build();
	}

	

}
