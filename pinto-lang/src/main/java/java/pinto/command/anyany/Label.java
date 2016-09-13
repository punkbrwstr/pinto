package pinto.command.anyany;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;

import pinto.command.ParameterizedCommand;
import pinto.data.AnyData;
import pinto.data.Data;
import pinto.time.Period;
import pinto.time.PeriodicRange;

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
