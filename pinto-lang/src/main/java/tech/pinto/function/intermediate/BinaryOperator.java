package tech.pinto.function.intermediate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.DoubleStream;

import tech.pinto.Indexer;
import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.function.ComposableFunction;

public class BinaryOperator extends ComposableFunction {

	protected final DoubleBinaryOperator operator;

	public BinaryOperator(String name, ComposableFunction previousFunction, Indexer indexer, DoubleBinaryOperator operator, String... args) {
		super(name, previousFunction, indexer, args);
		this.operator = operator;
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		int fixedCount = args.length > 0 ? Integer.parseInt(args[0]) : 1;
		if (stack.size() < fixedCount + 1) {
			throw new IllegalArgumentException("Not enough inputs for " + name.get());
		}
		ArrayDeque<EvaluableFunction> secondOperands = new ArrayDeque<>(stack.subList(0,fixedCount));
		List<EvaluableFunction> firstOperands = new ArrayList<>(stack.subList(fixedCount, stack.size()));
		stack.clear();
		for(int i = 0; i < firstOperands.size(); i++) {
			EvaluableFunction secondOperand = secondOperands.removeFirst();
			secondOperands.addLast(secondOperand);
			if(i >= secondOperands.size()) {
				secondOperand = secondOperand.clone();
			}
			stack.add(new EvaluableFunction(inputs -> join(inputs[1].toString(), inputs[0].toString(), toString()),
				inputs -> range -> {
					TimeSeries a = inputs[0].evaluate(range);
					TimeSeries b = inputs[1].evaluate(range);
					OfDouble bIterator = b.stream().iterator();
					DoubleStream outputStream = a.stream()
							.map(aValue -> operator.applyAsDouble(bIterator.nextDouble(), aValue));
					return outputStream;
				}, secondOperand, firstOperands.get(i)));
			}
		return stack;
	}

	public static FunctionHelp getHelp(String name, String desc) {
		return new FunctionHelp.Builder(name)
				.outputs("n - 1").description("Binary operator for " + desc
						+ ". Applies operation to fixed inputs (in order, with recycling) combined with each subsequent input.")
				.parameter("fixed operand count", "1", null)
				.build();
	}

}
