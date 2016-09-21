package tech.pinto.command;

import java.util.function.Function;

import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class SimpleCommand extends Command {

	private Function<PeriodicRange<?>, Data<?>> evaluationFunction;

	public SimpleCommand(Command c, int inputCount, int outputCount, Function<PeriodicRange<?>, Data<?>> evaluationFunction) {
		this(c.getName(),c.getInputType(),c.getOutputType(),inputCount,outputCount,evaluationFunction);
		inputStack.addFirst(c);
	}

	public SimpleCommand(String name, Class<? extends Data<?>> inputType, Class<? extends Data<?>> outputType,
			int inputCount, int outputCount, Function<PeriodicRange<?>, Data<?>> evaluationFunction) {
		super(name, inputType, outputType);
		this.inputCount = inputCount;
		this.outputCount = outputCount;
		this.evaluationFunction = evaluationFunction;
	}

	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		return evaluationFunction.apply(range);
	}

}
