package tech.pinto.function;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Pinto;
import tech.pinto.function.intermediate.Defined;

public class DefinedFunctionFactory implements FunctionFactory {

	private final ComposableFunction function;
	
	public DefinedFunctionFactory(ComposableFunction function) {
		this.function = function;
	}

	@Override
	public ComposableFunction build(String name, Pinto pinto, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		if(pinto == null) { // kluge.  only Defined calls it this way
			return (ComposableFunction) function.clone();
		} else {
			return new Defined(name, namespace, previousFunction, indexer);
		}
	}

}
