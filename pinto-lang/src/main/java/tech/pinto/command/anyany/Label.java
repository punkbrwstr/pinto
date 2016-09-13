package tech.pinto.command.anyany;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;

import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Label extends ParameterizedCommand<Object, AnyData, Object, AnyData> {

	public Label(String[] arguments) {
		super("label", AnyData.class, AnyData.class, arguments);
		inputCount = arguments.length;
		outputCount = inputCount;
	}

	@Override
	protected <P extends Period> ArrayDeque<AnyData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<AnyData> output = getInputData(range);
		final AtomicInteger i = new AtomicInteger(0);
		((ArrayDeque<? extends Data<?>>) output).stream().forEach( d -> d.setLabel(arguments[i.getAndIncrement()]));
		return output;
	}
	

}
