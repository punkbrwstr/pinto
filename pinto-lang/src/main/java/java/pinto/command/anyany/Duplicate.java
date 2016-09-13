package pinto.command.anyany;

import java.util.ArrayDeque;


import pinto.command.Command;
import pinto.data.AnyData;
import pinto.data.Data;
import pinto.time.Period;
import pinto.time.PeriodicRange;

public class Duplicate extends Command<Object, AnyData, Object, AnyData> {
	

	public Duplicate() {
		super("dup", AnyData.class, AnyData.class);
		inputCount = Integer.MAX_VALUE;
		outputCount = Integer.MAX_VALUE;
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.stream().mapToInt(Command::outputCount).sum() * 2;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected <P extends Period> ArrayDeque<AnyData> evaluate(PeriodicRange<P> range) {
//		ArrayDeque<Command<?,?,Object,AnyData>> dups = new ArrayDeque<>();
//		for(Command<?,?,Object,AnyData> c : inputStack) {
//			dups.addLast(c.clone());
//		}
//		inputStack.addAll(dups);
        inputStack.stream().map(Command::clone).forEach(inputStack::addLast);
		ArrayDeque output = new ArrayDeque<>();
		for(Data d : getInputData(range)) {
			output.addLast(d);
		}
		return output;
	}

}
