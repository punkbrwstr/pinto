package tech.pinto.function.supplier;

import java.util.LinkedList;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class Range extends ComposableFunction {

	public Range(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}
	
	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		int count = getArgs().length > 0 ? Integer.parseInt(getArgs()[0]) : 5;
		IntStream.range(0,count).mapToDouble(i -> (double)i).mapToObj(
				value -> new Column(inputs -> Double.toString(value),
						inputs -> range -> DoubleStream.iterate(value, r -> value).limit(range.size()))).forEach(stack::addFirst);;
		return stack;
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Sequence of integers from 0 to *i* ")
				.parameter("i","5",null)
				.outputs("n * m")
				.build();
	}

}
