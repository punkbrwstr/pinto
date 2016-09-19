package tech.pinto.command.anyany;

import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Label extends ParameterizedCommand {

	public Label(String[] arguments) {
		super("label", AnyData.class, AnyData.class, arguments);
		inputCount = arguments.length;
		outputCount = inputCount;
	}

	@Override
	public <P extends Period> AnyData evaluate(PeriodicRange<P> range) {
		Data<?> data = inputStack.removeLast().evaluate(range);
		data.setLabel(arguments[evaluationCount++]);
		return (AnyData) data;
	}
	

}
