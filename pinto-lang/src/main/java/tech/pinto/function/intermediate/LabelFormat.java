package tech.pinto.function.intermediate;


import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import tech.pinto.function.FunctionHelp;
import tech.pinto.time.PeriodicRange;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class LabelFormat extends ComposableFunction {
	
	
	public LabelFormat(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<Column> composeIndexed(LinkedList<Column> stack) {
		if(args.length != 0) {
			MessageFormat mf = new MessageFormat(args[0]);
			ArrayDeque<Column> temp = new ArrayDeque<>();
			while(!stack.isEmpty()) {
				Column old = stack.removeFirst();
			Function<Column[], Function<PeriodicRange<?>,DoubleStream>> seriesFunction = 
					c-> r-> old.getSeriesFunction().apply(c).apply(r);
				temp.addFirst(new Column(inputs -> mf.format(new Object[] {old.toString()}),
					seriesFunction,old.getInputs()));
			}
			temp.stream().forEach(stack::addFirst);
		}
		return stack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Formats labels according to supplied format string.")
				.parameter("format string ({0} for existing label)")
				.build();
	}

	

}
