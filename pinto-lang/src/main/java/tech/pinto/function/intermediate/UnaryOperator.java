package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import tech.pinto.TimeSeries;
import tech.pinto.function.Function;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.function.UnaryFunction;

public class UnaryOperator extends IntermediateFunction {

	protected final DoubleUnaryOperator operator;
	
	public UnaryOperator(String name, LinkedList<Function> inputs, DoubleUnaryOperator operator) {
		super(name,inputs);
		this.operator = operator;
		outputCount = inputStack.size();
	}

	@Override
	public Function getReference() {
		Function function = inputStack.removeFirst();
		return new UnaryFunction(joinWithSpaces(function.toString(),name),function, 
			f -> range -> {
				return new TimeSeries(range,this.toString(),f.evaluate(range).stream().map(operator));
			});
	}
	
	public static Supplier<FunctionHelp> getHelp(String name, String desc) {
		return () -> new FunctionHelp.Builder(name)
				//.inputs("double<sub>1</sub>, double<sub>2</sub>")
				.outputs("n")
				.description("Unary operator for " + desc + ". Applies operation to each input.")
				.build();
	}

}
