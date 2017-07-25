package tech.pinto.function.functions;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;

public class Only extends ComposableFunction {
	
	

	public Only(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

    public LinkedList<Column> compose() throws PintoSyntaxException {
    	LinkedList<Column> inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
    	LinkedList<Column> outputs = new LinkedList<>();
    	for(LinkedList<Column> indexedInputs : indexer.index(inputs)) {
    		outputs.addAll(apply(indexedInputs));
    	} 
    	return outputs;
    }
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Clears stack except for functions specified by indexing expression.")
				.outputs("Determined by index expression")
				.build();
	}

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		return stack;
	}

}
