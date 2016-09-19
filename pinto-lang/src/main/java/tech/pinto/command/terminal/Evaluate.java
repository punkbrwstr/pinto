package tech.pinto.command.terminal;

import java.time.LocalDate;

import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

public class Evaluate extends ParameterizedCommand {

	protected final PeriodicRange<?> range;

	public Evaluate(String[] arguments) {
		super("eval", AnyData.class, AnyData.class, arguments);
		LocalDate start = LocalDate.parse(arguments[0]);
		LocalDate end = LocalDate.parse(arguments[1]);
		Periodicity<?> p =  Periodicities.get(arguments.length > 2 ? arguments[2] : "B");
		this.range = p.range(start, end, false);

		inputCount = Integer.MAX_VALUE;
		outputCount = Integer.MAX_VALUE;
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size();
	}

	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		// argument range is null
		return inputStack.removeFirst().evaluate(this.range);
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	

}
