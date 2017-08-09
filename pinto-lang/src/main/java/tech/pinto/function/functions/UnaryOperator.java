package tech.pinto.function.functions;

import java.util.LinkedList;


import java.util.function.DoubleUnaryOperator;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class UnaryOperator extends ComposableFunction {
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Calculates {0} of inputs.");

	protected final DoubleUnaryOperator operator;
	
	public UnaryOperator(String name, ComposableFunction previousFunction, Indexer indexer, DoubleUnaryOperator operator) {
		super(name, previousFunction, indexer);
		this.operator = operator;
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		for (Column col : inputStack) {
			stack.add(new Column(inputs -> join(inputs[0].toString(), toString()),
				inputs -> range -> inputs[0].getCells(range).map(operator), col));
		}
	}
}
