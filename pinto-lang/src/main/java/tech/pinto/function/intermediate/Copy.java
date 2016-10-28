package tech.pinto.function.intermediate;

import java.util.ArrayDeque;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Copy extends ComposableFunction {
	
	
	public Copy(String name, ComposableFunction previousFunction, Indexer indexer, String[] args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		int times = args.length == 0 ? 2 : Integer.parseInt(args[0]);
		ArrayDeque<EvaluableFunction> temp = new ArrayDeque<>();
        stack.stream().forEach(temp::addFirst);
        for(int i = 0; i < times - 1; i++) {
        	temp.stream().map(EvaluableFunction::clone).forEach(stack::addFirst);
        }
		return stack;
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Copies stack inputs *m* times")
				.parameter("m","2",null)
				.outputs("n * m")
				.build();
	}
}
