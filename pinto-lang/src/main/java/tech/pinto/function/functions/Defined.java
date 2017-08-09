package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.function.ComposableFunction;

public class Defined extends ComposableFunction {
	
	private final Namespace namespace;

	public Defined(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.namespace = namespace;
	}

	@Override
	public LinkedList<Column> compose() {
		LinkedList<Column> outputs = new LinkedList<>();
		ComposableFunction clone = namespace.getFunction(name, null, null, null);
		ComposableFunction head = clone.getHead();
		LinkedList<Column> inputs = previousFunction.get().compose();
		for(LinkedList<Column> definedInputs : indexer.index(inputs)) {
			head.setInputs(head.getIndexer().index(definedInputs).get(0));
           	outputs.addAll(clone.compose());
           	outputs.addAll(definedInputs);
		}
		outputs.addAll(inputs);
		return outputs;
	}
}
