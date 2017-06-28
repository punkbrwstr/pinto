package tech.pinto.function.intermediate;

import java.util.LinkedList;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;

public class Comment extends ComposableFunction {

	public Comment(String name, ComposableFunction previousFunction, Indexer indexer, String[] args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<Column> composeIndexed(LinkedList<Column> stack) {
		return stack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("0")
				.description("Just a comment.")
				.parameter("Comment text",null, null)
				.build();
	}

}
