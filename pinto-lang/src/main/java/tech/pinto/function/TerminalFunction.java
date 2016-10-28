package tech.pinto.function;

import java.util.LinkedList;

import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.TimeSeries;

public class TerminalFunction extends ComposableFunction {
	
	protected final Namespace namespace;

	public TerminalFunction(String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
		this.namespace = namespace;
	}

	public Optional<LinkedList<TimeSeries>> getTimeSeries() throws PintoSyntaxException {
		return Optional.empty();
	}

	public Optional<String> getText() throws PintoSyntaxException {
		return Optional.empty();
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		throw new UnsupportedOperationException();
	}


}
