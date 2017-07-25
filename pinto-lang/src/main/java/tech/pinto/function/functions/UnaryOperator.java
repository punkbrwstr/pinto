package tech.pinto.function.functions;

import java.util.LinkedList;


import java.util.function.DoubleUnaryOperator;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class UnaryOperator extends ComposableFunction {

	protected final DoubleUnaryOperator operator;
	
	public UnaryOperator(String name, ComposableFunction previousFunction, Indexer indexer, DoubleUnaryOperator operator) {
		super(name, previousFunction, indexer);
		this.operator = operator;
	}

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		LinkedList<Column> outputs = new LinkedList<>();
		for (Column function : stack) {
			outputs.add(new Column(inputs -> join(inputs[0].toString(), toString()),
				inputs -> range -> inputs[0].getSeries(range).get().map(operator), function));
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
