package tech.pinto.function.supplier;

import java.util.LinkedList;

import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.EvaluableFunction;

public class Moon extends ComposableFunction {

	public Moon(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		stack.addFirst(new EvaluableFunction(inputs -> toString(),
				inputs -> range -> range.dates().stream().mapToDouble(d -> new tech.pinto.tools.MoonPhase(d).getPhase())));
		return stack;
	}
}
