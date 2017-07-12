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

	public Head(Indexer indexer) {
		super(Optional.empty(),Optional.empty(),indexer, ParameterType.no_arguments);
	}

	@Override
	public LinkedList<Column> compose() throws PintoSyntaxException {
    	LinkedList<Column> inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
    	LinkedList<Column> outputs = indexer.index(inputs);
    	definedTail.ifPresent(dt -> dt.addSkippedInputs(inputs));
    	return outputs;
	}
	
	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		return stack;
	}
	
	public void setDefinedTail(Defined definedTail) {
		this.definedTail = Optional.of(definedTail);
	}

	public void setPrevious(ComposableFunction previousFunction) {
		this.previousFunction = Optional.of(previousFunction);
	}
}
