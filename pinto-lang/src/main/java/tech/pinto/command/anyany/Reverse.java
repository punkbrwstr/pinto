package tech.pinto.command.anyany;

import java.util.ArrayDeque;

import tech.pinto.command.Command;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Reverse extends Command<Object, AnyData, Object, AnyData> {

	public Reverse() {
		super("rev", AnyData.class, AnyData.class);
		inputCount = Integer.MAX_VALUE;
		outputCount = Integer.MAX_VALUE;
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.stream().mapToInt(Command::outputCount).sum();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected <P extends Period> ArrayDeque<AnyData> evaluate(PeriodicRange<P> range) {
		ArrayDeque output = new ArrayDeque<>();
		for(Data d : getInputData(range)) {
			output.addFirst(d);
		}
		return output;
	}

}
