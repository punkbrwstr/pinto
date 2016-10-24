package tech.pinto.function.intermediate;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.DoubleStream;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ReferenceFunction;
import tech.pinto.function.LambdaFunction;
import tech.pinto.function.Function;

public class BinaryOperator extends ReferenceFunction {

	protected final DoubleBinaryOperator operator;
	protected final int fixedCount;
	protected Boolean emptyInputs = null;
	protected ArrayDeque<Function> fixed;
	protected final HashMap<Integer, TimeSeries> fixedData = new HashMap<>();

	public BinaryOperator(String name, LinkedList<Function> inputs, DoubleBinaryOperator operator, String...args) {
		super(name, inputs);
		this.operator = operator;
		fixedCount = args.length > 0 ? Integer.parseInt(args[0]) : 1;
	}

	@Override
	public Function getReference() {
		if(emptyInputs) {
			return this;
		}
		if (fixed == null) {
			if (inputStack.size() < fixedCount + 1) {
				throw new IllegalArgumentException("Not enough inputs for " + name);
			}
			fixed = new ArrayDeque<>();
			for(int i = 0; i < fixedCount; i++) {
				fixed.addLast(inputStack.removeFirst());
			}
		}
		Function functionA = fixed.removeFirst();
		fixed.addLast(functionA);
		Function functionB = inputStack.removeFirst();
		System.out.println("Binary operator creating lambda with a " + functionA.cloneOrNot + " and a " + functionB.cloneOrNot);
		return new LambdaFunction(f -> join(f.getStack().get(1).toString(), f.getStack().get(0).toString(), toString()),
				inputs -> range -> {
					Function myFunctionA = inputs.removeFirst(), myFunctionB = inputs.removeFirst();
					System.out.println("Binary operator operating on a " + myFunctionA.cloneOrNot + " and a " + myFunctionB.cloneOrNot);
					Integer functionRangeHash = Objects.hash(myFunctionA,range);
					if (!fixedData.containsKey(functionRangeHash)) {
						fixedData.put(functionRangeHash, myFunctionA.evaluate(range));
					}
					TimeSeries a = fixedData.get(functionRangeHash).clone();
					TimeSeries b = myFunctionB.evaluate(range);
					OfDouble bIterator = b.stream().iterator();
					DoubleStream outputStream = a.stream()
							.map(aValue -> operator.applyAsDouble(bIterator.nextDouble(), aValue));
					return outputStream;
				}, functionA, functionB);
	}

	public static FunctionHelp getHelp(String name, String desc) {
		return new FunctionHelp.Builder(name)
				.outputs("n - 1").description("Binary operator for " + desc
						+ ". Applies operation to fixed inputs (in order, with recycling) combined with each subsequent input.")
				.parameter("fixed operand count", "1", null)
				.build();
	}

	@Override
	public int getOutputCount() {
		if(emptyInputs == null) {
			if(inputStack.size()==0) {
				emptyInputs = true;
				return 1;
			} else {
				emptyInputs = false;
			}
		}
		return inputStack.size() - fixedCount;
	}

}
