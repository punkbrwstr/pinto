package tech.pinto.function.functions;


import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Label extends ComposableFunction {
	
	
	public Label(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		List<String> l = Arrays.asList(getArgs());
		List<String> labels = l.subList(0, Math.min(l.size(), stack.size()));
		ArrayDeque<Column> temp = new ArrayDeque<>();
		for(int i = 0; i < labels.size(); i++) {
			final int index = labels.size() - i - 1;
			Column old = stack.removeFirst();
//			Function<Column[], Function<PeriodicRange<?>,DoubleStream>> seriesFunction = 
//					c-> r-> old.getSeriesFunction().apply(c).apply(r).get();
			temp.addFirst(new Column(old.getInputs(),Optional.of(inputs -> labels.get(index)), old.getSeriesFunction()));
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
