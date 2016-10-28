package tech.pinto.function.intermediate;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;

public class Only extends ComposableFunction {
	
	

	public Only(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		String indexString = Stream.of(args).collect(Collectors.joining(","));
		try {
			return new Indexer(indexString).index(stack);
		} catch (PintoSyntaxException e) {
			throw new RuntimeException("Syntax error in arguments of " + name + ".",e);
		}
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Clears stack except for functions specified by indexing expression.")
				.parameter("Index expression")
				.outputs("Determined by index expression")
				.build();
	}

}
