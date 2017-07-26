package tech.pinto.function;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.Column;

abstract public class TerminalFunction extends ComposableFunction {

	protected final Namespace namespace;

	public TerminalFunction(ParameterType parameterType, String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer, parameterType);
		this.namespace = namespace;
	}

	public TerminalFunction(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer, ParameterType.arguments_optional);
		this.namespace = namespace;
	}

	abstract public Table getTable() throws PintoSyntaxException;

	protected Table createTextColumn(String s) {
		LinkedList<Column> ll = new LinkedList<>();
		ll.add(new Column(s));
		return new Table(ll,Optional.empty());
	}

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		return stack;
	}
	
	

}
