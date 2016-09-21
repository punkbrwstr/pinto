package tech.pinto.command.anyany;

import java.util.ArrayDeque;

import tech.pinto.command.Command;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Copy extends ParameterizedCommand {
	
	private final int times;

	public Copy(String...args) {
		super("copy", AnyData.class, AnyData.class, args);
		inputCount = args.length == 0 ? Integer.MAX_VALUE :
			Integer.parseInt(args[0]) == -1 ? Integer.MAX_VALUE : Integer.parseInt(args[0]);
		times = args.length < 2 ? 2 : Integer.parseInt(args[1]);
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size() * times;
		ArrayDeque<Command> temp = new ArrayDeque<>();
        inputStack.stream().forEach(temp::addLast);
        for(int i = 0; i < times - 1; i++) {
        	temp.stream().map(Command::clone).forEach(inputStack::addLast);
        }
        
	}
	
	@Override public Command getReference() {
		return inputStack.removeFirst();
	}




	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		// never gets called bc it passes on references to inputs
		throw new UnsupportedOperationException();
	}

}
