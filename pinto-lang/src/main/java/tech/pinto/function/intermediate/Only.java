package tech.pinto.function.intermediate;

import java.util.LinkedList;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;

public class Only extends ComposableFunction {
	
	

	public Only(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

    public LinkedList<Column> compose() throws PintoSyntaxException {
    	LinkedList<Column> inputs = previousFunction.isPresent() ? previousFunction.get().compose() : new LinkedList<>();
    	LinkedList<Column> outputs = new LinkedList<>();
    	int i = 0;
    	do {
    		try {
    			outputs.addAll(indexer.index(inputs));
    		} catch(PintoSyntaxException pse) {
    			if(i > 0) {
    				break;
    			} else {
    				throw pse;
    			}
    		}
    		i++;
    	} while(indexer.isRepeated() && inputs.size() > 0);
    	return outputs;
    }
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Clears stack except for functions specified by indexing expression.")
				.outputs("Determined by index expression")
				.build();
	}

}
