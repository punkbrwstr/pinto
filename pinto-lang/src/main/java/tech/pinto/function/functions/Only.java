package tech.pinto.function.functions;

import java.util.LinkedList;


import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class Only extends ComposableFunction {

	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Clears stack except for inputs");

	public Only(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

    public LinkedList<Column> compose() {
    	LinkedList<Column> outputs = new LinkedList<>();
    	for(LinkedList<Column> indexedInputs : indexer.index(previousFunction.get().compose())) {
    		outputs.addAll(indexedInputs);
    	} 
    	return outputs;
    }
}
