package tech.pinto.function;

import java.util.LinkedList;

import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Column;
import tech.pinto.ColumnValues;

public class TerminalFunction extends ComposableFunction {
	
	protected final Namespace namespace;

	public TerminalFunction(String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
		this.namespace = namespace;
	}

	public Optional<LinkedList<ColumnValues>> getTimeSeries() throws PintoSyntaxException {
		return Optional.empty();
	}

	public Optional<String> getText() throws PintoSyntaxException {
		return Optional.empty();
	}

	@Override
	public LinkedList<Column> composeIndexed(LinkedList<Column> stack) {
		throw new UnsupportedOperationException();
	}


}
