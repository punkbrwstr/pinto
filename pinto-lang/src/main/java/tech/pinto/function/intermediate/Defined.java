package tech.pinto.function.intermediate;

import java.util.LinkedList;

import com.google.common.collect.ObjectArrays;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.EvaluableFunction;

public class Defined extends ComposableFunction {
	
	private final Namespace namespace;
	private LinkedList<EvaluableFunction> skippedInputs = new LinkedList<>();

	public Defined(String name, Namespace namespace, ComposableFunction previousFunction, 
			Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
		this.namespace = namespace;
	}

	@Override
	public LinkedList<EvaluableFunction> compose() throws PintoSyntaxException {
		ComposableFunction clone = namespace.getFunction(name.get(), null, null, null);
		if(!previousFunction.get().isHead()) {
			Head head = (Head) clone.getHead();
			head.setPrevious(previousFunction.get());
			head.setDefinedTail(this);
            if(!indexer.isEverything()) {
			    head.setIndexer(indexer);
            }
			if(args.length > 0) {
				head.setArgs(ObjectArrays.concat(head.getArgs(), args, String.class));
			}
		}
		LinkedList<EvaluableFunction> outputs = clone.compose();
		outputs.addAll(skippedInputs);
		return outputs;
	}
	
	public void addSkippedInputs(LinkedList<EvaluableFunction> skippedInputs) {
		this.skippedInputs.addAll(skippedInputs);
	}

	@Override
	protected Object clone() {
		Defined clone = (Defined) super.clone();
		clone.skippedInputs = new LinkedList<>();
		return clone;
	}
	
	

}
