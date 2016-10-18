package tech.pinto.function.intermediate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.function.UnaryFunction;
import tech.pinto.function.Function;
import tech.pinto.time.PeriodicRange;

public class BinaryOperator extends IntermediateFunction {

	protected final DoubleBinaryOperator operator;
	protected Function fixed;
	protected final HashMap<PeriodicRange<?>, TimeSeries> fixedData = new HashMap<>();

	public BinaryOperator(String name, LinkedList<Function> inputs, DoubleBinaryOperator operator) {
		super(name, inputs);
		this.operator = operator;
		outputCount = inputStack.size() - 1;
	}

	@Override
	public Function getReference() {
		if (fixed == null) {
			if (inputStack.size() < 2) {
				throw new IllegalArgumentException("not enough inputs for " + name);
			}
			fixed = inputStack.removeFirst();
		}
		Function functionB = inputStack.removeFirst();
		return new UnaryFunction(joinWithSpaces(functionB.toString(), fixed.toString(), name), functionB,
				f -> range -> {
					if (!fixedData.containsKey(range)) {
						fixedData.put(range, fixed.evaluate(range));
					}
					TimeSeries a = fixedData.get(range).clone();
					TimeSeries b = f.evaluate(range);
					OfDouble bIterator = b.stream().iterator();
					DoubleStream outputStream = a.stream()
							.map(aValue -> operator.applyAsDouble(bIterator.nextDouble(), aValue));
					return new TimeSeries(range, joinWithSpaces(functionB.toString(), fixed.toString(), name),
							outputStream);
				});
	}

	public static Supplier<FunctionHelp> getHelp(String name, String desc) {
		return () -> new FunctionHelp.Builder(name)
				// .inputs("double<sub>1</sub>, double<sub>2</sub>")
				.outputs("n - 1").description("Binary operator for " + desc
						+ ". Applies operation to first input combined with each subsequent input.")
				.build();
	}

}
