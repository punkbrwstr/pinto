package pinto.command.terminal;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.stream.Collectors;

import pinto.command.ParameterizedCommand;
import pinto.data.AnyData;
import pinto.time.Period;
import pinto.time.PeriodicRange;
import pinto.time.Periodicities;
import pinto.time.Periodicity;

public class Evaluate extends ParameterizedCommand<Object, AnyData, Object, AnyData> {

	public Evaluate(String[] arguments) {
		super("eval", AnyData.class, AnyData.class, arguments);
		inputCount = Integer.MAX_VALUE;
	}

	@Override
	protected <P extends Period> ArrayDeque<AnyData> evaluate(PeriodicRange<P> range) {
		// passed range is null
		LocalDate start = LocalDate.parse(arguments[0]);
		LocalDate end = LocalDate.parse(arguments[1]);
		Periodicity<?> p =  Periodicities.get(arguments.length > 2 ? arguments[2] : "B");
		PeriodicRange<?> r = p.range(start, end, false);
		return inputStack.stream().flatMap(c -> c.getOutputData(r).stream())
				.collect(Collectors.toCollection(() -> new ArrayDeque<>()));
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	

}
