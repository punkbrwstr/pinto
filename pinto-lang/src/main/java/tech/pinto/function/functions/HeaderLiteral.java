package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class HeaderLiteral extends ComposableFunction {

	private final String value;
	
	public HeaderLiteral(ComposableFunction previousFunction, Indexer indexer, String value) {
		super(value,previousFunction, indexer);
		this.value = value;
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		stack.addFirst(new Column(value));
	}
	
	public String getValue() {
		return value;
	}
}
