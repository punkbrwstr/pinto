package tech.pinto.function.intermediate;

import java.util.LinkedList;


import java.util.function.DoubleUnaryOperator;

import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;

public class UnaryOperator extends ComposableFunction {

	protected final DoubleUnaryOperator operator;
	
	public UnaryOperator(String name, ComposableFunction previousFunction, Indexer indexer, DoubleUnaryOperator operator, String... args) {
		super(name, previousFunction, indexer, args);
		this.operator = operator;
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		LinkedList<EvaluableFunction> outputs = new LinkedList<>();
		for (EvaluableFunction function : stack) {
			outputs.add(new EvaluableFunction(inputs -> join(inputs[0].toString(), toString()),
				inputs -> range -> inputs[0].evaluate(range).stream().map(operator), function));
		}
		return outputs;
	}

	public static FunctionHelp getHelp(String name, String desc) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Unary operator for " + desc + ". Applies operation to each input.")
				.build();
	}

}
