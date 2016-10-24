package tech.pinto.function.intermediate;

import java.util.LinkedList;


import java.util.function.DoubleUnaryOperator;

import tech.pinto.function.Function;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ReferenceFunction;
import tech.pinto.function.LambdaFunction;

public class UnaryOperator extends ReferenceFunction {

	protected final DoubleUnaryOperator operator;
	
	public UnaryOperator(String name, LinkedList<Function> inputs, DoubleUnaryOperator operator) {
		super(name,inputs);
		this.operator = operator;
	}

	@Override
	public Function getReference() {
		Function function = inputStack.removeFirst();
		return new LambdaFunction(f -> join(f.getStack().getFirst().toString(),toString()), 
			f -> range -> f.removeFirst().evaluate(range).stream().map(operator), function);
	}
	
	public static FunctionHelp getHelp(String name, String desc) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Unary operator for " + desc + ". Applies operation to each input.")
				.build();
	}

	@Override
	public int getOutputCount() {
		return inputStack.size();
	}

}
