package tech.pinto.function.intermediate;


import java.text.MessageFormat;
import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class LabelFormat extends ComposableFunction {
	
	
	public LabelFormat(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		if(args.length != 0) {
			MessageFormat mf = new MessageFormat(args[0]);
			for (EvaluableFunction ev : stack) {
				final Object[] currentLabel = new Object[]{ev.toString()};
				ev.setLabeller(inputs -> mf.format(currentLabel));
			}
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
