package tech.pinto.function.functions;

import java.util.ArrayDeque;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.DoubleStream;

import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.Column;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;

public class BinaryOperator extends ComposableFunction {
	
	static private final Parameters.Builder PARAMETER_BUILDER = new Parameters.Builder()
			.add("right_count", "1", "Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.)");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETER_BUILDER.build())
			.description("{0} fixed inputs (in order, with recycling) {1} each subsequent input.");

	protected final DoubleBinaryOperator operator;

	public BinaryOperator(String name, ComposableFunction previousFunction, Indexer indexer, DoubleBinaryOperator operator) {
		super(name, previousFunction, indexer);
		this.operator = operator;
		this.parameters = Optional.of(PARAMETER_BUILDER.build());
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		int rightSide = Integer.parseInt(parameters.get().getArgument("right_count"));
		if (stack.size() < rightSide + 1) {
			throw new IllegalArgumentException("Not enough inputs for " + name);
		}
		ArrayDeque<Column> secondOperands = new ArrayDeque<>(stack.subList(0,rightSide));
		List<Column> firstOperands = new ArrayList<>(stack.subList(rightSide, stack.size()));
		stack.clear();
		for(int i = 0; i < firstOperands.size(); i++) {
			Column secondOperand = secondOperands.removeFirst();
			secondOperands.addLast(secondOperand);
			if(i >= secondOperands.size()) {
				secondOperand = secondOperand.clone();
			}
			stack.add(new Column(inputs -> join(inputs[1].toString(), inputs[0].toString(), toString()),
				inputs -> range -> {
					DoubleStream a = inputs[0].getCells(range);
					DoubleStream b = inputs[1].getCells(range);
					OfDouble bIterator = b.iterator();
					DoubleStream outputStream = a.map(aValue -> operator.applyAsDouble(bIterator.nextDouble(), aValue));
					return outputStream;
				}, secondOperand, firstOperands.get(i)));
			}
	}

}
