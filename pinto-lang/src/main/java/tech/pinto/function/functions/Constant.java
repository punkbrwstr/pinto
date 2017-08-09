package tech.pinto.function.functions;

import java.util.LinkedList;

import java.util.stream.DoubleStream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Constant extends ComposableFunction {

	private final double value;
	
	public Constant(ComposableFunction previousFunction, Indexer indexer, double value) {
		super(Double.toString(value),previousFunction, indexer);
		this.value = value;
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		stack.addFirst(new Column(inputs -> Double.toString(value),
				inputs -> range -> DoubleStream.iterate(value, r -> value).limit(range.size())));
	}
}
