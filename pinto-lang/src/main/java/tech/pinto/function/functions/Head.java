package tech.pinto.function.functions;

import java.util.LinkedList;

import java.util.Optional;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.ParameterType;

public class Head extends ComposableFunction {

	private Optional<LinkedList<Column>> inputs = Optional.empty();

	public Head(Indexer indexer) {
		super(Optional.empty(), Optional.empty(), indexer, ParameterType.no_arguments);
	}

	@Override
	public LinkedList<Column> compose() throws PintoSyntaxException {
		LinkedList<Column> outputs = new LinkedList<>();
		if (inputs.isPresent()) {
			for (LinkedList<Column> output : indexer.index(inputs.get())) {
				outputs.addAll(output);
			}
		}
		return outputs;
	}

	public void setInputs(LinkedList<Column> inputs) {
		this.inputs = Optional.of(inputs);
	}

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		return stack;
	}
}
