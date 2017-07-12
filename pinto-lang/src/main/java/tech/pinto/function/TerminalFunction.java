package tech.pinto.function;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.time.PeriodicRange;
import tech.pinto.Column;
import tech.pinto.ColumnValues;

abstract public class TerminalFunction extends ComposableFunction {
	
	protected final Namespace namespace;

	public TerminalFunction(ParameterType parameterType,String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer) {
		super(name,previousFunction, indexer, parameterType);
		this.namespace = namespace;
	}

	public TerminalFunction(String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer) {
		super(name,previousFunction, indexer, ParameterType.arguments_optional);
		this.namespace = namespace;
	}

	abstract public LinkedList<ColumnValues> getColumnValues() throws PintoSyntaxException;
	
	public Optional<PeriodicRange<?>> getRange() throws PintoSyntaxException {
		return Optional.empty();
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		throw new UnsupportedOperationException();
	}

	protected LinkedList<ColumnValues> createTextColumn(String s) {
		LinkedList<ColumnValues> ll = new LinkedList<>();
		ll.add(new ColumnValues(Optional.of(s),Optional.empty()));
		return ll;
	}

}
