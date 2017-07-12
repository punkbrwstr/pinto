package tech.pinto.function.intermediate;

import java.util.LinkedList;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.ParameterType;

public class Defined extends ComposableFunction {
	
	private final Namespace namespace;
	private LinkedList<Column> skippedInputs = new LinkedList<>();

	public Defined(String name, Namespace namespace, ComposableFunction previousFunction, 
			Indexer indexer) {
		super(name, previousFunction, indexer, ParameterType.no_arguments);
		this.namespace = namespace;
	}

	@Override
	public LinkedList<Column> compose() throws PintoSyntaxException {
		ComposableFunction clone = namespace.getFunction(name.get(), null, null, null);
		//Optional<ComposableFunction> second = clone.getSecondToHead();
		if(!previousFunction.get().isHead()) {
			Head head = (Head) clone.getHead();
			head.setPrevious(previousFunction.get());
			head.setDefinedTail(this);
            if(!indexer.isEverything()) {
			    head.setIndexer(indexer);
            }
		}
		LinkedList<Column> outputs = new LinkedList<>();
		outputs.addAll(clone.compose());
		outputs.addAll(skippedInputs);
		return outputs;
	}
	
	public void addSkippedInputs(LinkedList<Column> skippedInputs) {
		this.skippedInputs.addAll(skippedInputs);
	}

	@Override
	protected Object clone() {
		Defined clone = (Defined) super.clone();
		clone.skippedInputs = new LinkedList<>();
		return clone;
	}

	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		return stack;
	}
	
	

}
