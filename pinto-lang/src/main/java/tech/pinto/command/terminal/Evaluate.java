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

	public Evaluate(String[] args) {
		super("eval", AnyData.class, AnyData.class, args);
		Periodicity<?> p =  Periodicities.get(args.length > 2 ? args[2] : "B");
		LocalDate start = args.length > 0 ? LocalDate.parse(args[0]) : 
							p.from(LocalDate.now()).previous().endDate();
		LocalDate end = args.length > 1 ? LocalDate.parse(args[1]) : 
							p.from(LocalDate.now()).previous().endDate();
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
