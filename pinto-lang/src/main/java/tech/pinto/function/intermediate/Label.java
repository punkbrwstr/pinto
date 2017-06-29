package tech.pinto.function.intermediate;


import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import tech.pinto.function.FunctionHelp;
import tech.pinto.time.PeriodicRange;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Label extends ComposableFunction {
	
	
	public Label(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<Column> composeIndexed(LinkedList<Column> stack) {
		List<String> labels = Arrays.asList(args);
		Collections.reverse(labels);
		ArrayDeque<Column> temp = new ArrayDeque<>();
		for(int i = 0; i < labels.size() && stack.size() > 0; i++) {
			final int index = i;
			Column old = stack.removeFirst();
			Function<Column[], Function<PeriodicRange<?>,DoubleStream>> seriesFunction = 
					c-> r-> old.getSeriesFunction().apply(c).apply(r);
			temp.addFirst(new Column(inputs -> labels.get(index),
					seriesFunction,old.getInputs()));
		}
       	temp.stream().forEach(stack::addFirst);
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
