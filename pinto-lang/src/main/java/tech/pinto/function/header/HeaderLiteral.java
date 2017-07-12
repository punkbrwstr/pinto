package tech.pinto.function.header;

import java.util.LinkedList;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.ParameterType;

public class HeaderLiteral extends ComposableFunction {

	private final String value;
	
	public HeaderLiteral(ComposableFunction previousFunction, Indexer indexer, String value) {
		super(value,previousFunction, indexer, ParameterType.no_arguments);
		this.value = value;
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		stack.addFirst(new Column(inputs -> value));
		return stack;
	}
	
	public String getValue() {
		return value;
	}
}
