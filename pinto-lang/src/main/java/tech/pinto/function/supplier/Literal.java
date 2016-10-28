package tech.pinto.function.supplier;

import java.util.LinkedList;
import java.util.stream.DoubleStream;

import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.EvaluableFunction;

public class Literal extends ComposableFunction {

	private final double value;
	
	public Literal(ComposableFunction previousFunction, Indexer indexer, double value) {
		super(Double.toString(value), previousFunction, indexer);
		this.value = value;
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		stack.addFirst(new EvaluableFunction(inputs -> Double.toString(value),
				inputs -> range -> DoubleStream.iterate(value, r -> value).limit(range.size())));
		return stack;
	}
}
