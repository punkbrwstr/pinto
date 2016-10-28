package tech.pinto.function;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Pinto;

@FunctionalInterface
public interface ComposableFunctionFactory {
	
	public ComposableFunction build(String name, Pinto pinto, Namespace namespace, ComposableFunction previousFunction,
								Indexer indexer, String...arguments);

}
