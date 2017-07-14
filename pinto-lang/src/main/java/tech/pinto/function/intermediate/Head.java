package tech.pinto.function.intermediate;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.ParameterType;

public class Head extends ComposableFunction {

	private Optional<Defined> definedTail = Optional.empty();
	private LinkedList<Column> inputs = null;
	private Optional<Indexer> preIndexer = Optional.empty();

	public Head(Indexer indexer) {
		super(Optional.empty(), Optional.empty(), indexer, ParameterType.no_arguments);
	}

	public boolean hasInputs() {
		return inputs == null || inputs.size() > 0;
	}

	@Override
	public LinkedList<Column> compose() throws PintoSyntaxException {
		if (inputs == null) {
			inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
		}
		LinkedList<Column> outputs = null;
		try {
			LinkedList<Column> actualInputs;
			if(preIndexer.isPresent()) {
				actualInputs = preIndexer.get().index(inputs);
				outputs = indexer.index(actualInputs);
				inputs.addAll(actualInputs);
			} else {
				actualInputs = inputs;
				outputs = indexer.index(inputs);
			}
		} catch (PintoSyntaxException pse) {
			if (inputs.size() > 0) {
				definedTail.ifPresent(dt -> dt.addSkippedInputs(inputs));
				inputs.clear();
				outputs = new LinkedList<>();
			} else {
				throw pse;
			}
		}

		if (preIndexer.isPresent() && ! preIndexer.get().isRepeated()) {
			definedTail.ifPresent(dt -> dt.addSkippedInputs(inputs));
			inputs.clear();
		}
		return outputs;
	}
	
	public void setPreIndexer(Indexer indexer) {
		this.preIndexer = Optional.of(indexer);
	}

	public void setDefinedTail(Defined definedTail) {
		this.definedTail = Optional.of(definedTail);
	}

	public void setPrevious(ComposableFunction previousFunction) {
		this.previousFunction = Optional.of(previousFunction);
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		return stack;
	}
}
