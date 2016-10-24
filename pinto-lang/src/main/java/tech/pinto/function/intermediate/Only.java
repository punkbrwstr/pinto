package tech.pinto.function.intermediate;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Indexer;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.Function;
import tech.pinto.function.ReferenceFunction;

public class Only extends ReferenceFunction {
	
	private LinkedList<Function> outputs;
	
	public Only(String name, LinkedList<Function> inputs, String...args) {
		super(name, inputs, args);
		String indexString = Stream.of(args).collect(Collectors.joining(","));
		try {
			outputs = new Indexer(indexString, inputStack).index(inputStack);
		} catch (PintoSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Override public Function getReference() {
		return outputs.removeFirst();
	}
	
	@Override public Only clone() {
		Only clone = (Only) super.clone();
		clone.outputs = new LinkedList<>();
		outputs.stream().map(Function::clone).forEach(clone.outputs::addLast);
		return clone;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.description("Clears stack except for functions specified by indexing expression.")
				.parameter("Index expression")
				.outputs("Determined by index expression")
				.build();
	}

	@Override
	public int getOutputCount() {
		return outputs.size();
	}

	
}
