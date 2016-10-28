package tech.pinto.function.intermediate;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.EvaluableFunction;

public class Head extends ComposableFunction {
	
	private Optional<Defined> definedTail = Optional.empty();

	public Head(Indexer indexer) {
		super(Optional.empty(), Optional.empty(), indexer);
	}

	@Override
	public LinkedList<EvaluableFunction> compose() throws PintoSyntaxException {
    	LinkedList<EvaluableFunction> inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
    	LinkedList<EvaluableFunction> outputs = indexer.index(inputs);
    	definedTail.ifPresent(dt -> dt.addSkippedInputs(inputs));
    	return outputs;
	}
	
	public void setDefinedTail(Defined definedTail) {
		this.definedTail = Optional.of(definedTail);
	}
	
	

}
