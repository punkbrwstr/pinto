package tech.pinto.function;

import com.google.common.collect.ObjectArrays;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Pinto;

public class PintoFunctionFactory implements ComposableFunctionFactory {

	private final ComposableFunction function;
	
	public PintoFunctionFactory(ComposableFunction function) {
		this.function = function;
	}

	@Override
	public ComposableFunction build(String name, Pinto pinto, Namespace namespace, ComposableFunction previousFunction, Indexer indexer,
			String... arguments) {
		ComposableFunction clone = (ComposableFunction) function.clone();
		ComposableFunction head = clone.getHead();
		head.setPrevious(previousFunction);
		if(arguments.length > 0) {
			head.args = ObjectArrays.concat(head.args, arguments, String.class);
		}
		return clone;
	}

}
