package tech.pinto.command.anyany;



import tech.pinto.command.Command;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Roll extends ParameterizedCommand {
	

	public Roll(String[] arguments) {
		super("roll", AnyData.class, AnyData.class, arguments);
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size();
		int timesToRoll = arguments.length > 0 ? Integer.parseInt(arguments[0]) : 1;
		for(int i = 0; i < timesToRoll; i++) {
			inputStack.addFirst(inputStack.removeLast());
		}
	}
	
	@Override public Command getReference() {
		return inputStack.removeFirst();
	}


	@Override
	public <P extends Period> AnyData evaluate(PeriodicRange<P> range) {
		// never gets called bc it passes on references to inputs
		throw new UnsupportedOperationException();
	}
	

}
