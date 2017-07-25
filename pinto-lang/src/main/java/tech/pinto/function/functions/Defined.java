package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.ParameterType;

public class Defined extends ComposableFunction {
	
	private final Namespace namespace;

	public Defined(String name, Namespace namespace, ComposableFunction previousFunction, 
			Indexer indexer) {
		super(name, previousFunction, indexer, ParameterType.no_arguments);
		this.namespace = namespace;
	}

	@Override
	public LinkedList<Column> compose() throws PintoSyntaxException {
		LinkedList<Column> outputs = new LinkedList<>();
		ComposableFunction clone = namespace.getFunction(name.get(), null, null, null);
		Head head = (Head) clone.getHead();
		LinkedList<Column> inputs = previousFunction.get().compose();
		for(LinkedList<Column> definedInputs : indexer.index(inputs)) {
			head.setInputs(definedInputs);
           	outputs.addAll(clone.compose());
           	outputs.addAll(definedInputs);
		}
		outputs.addAll(inputs);
		return outputs;
	}
	
	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		return stack;
	}
	
	

}
