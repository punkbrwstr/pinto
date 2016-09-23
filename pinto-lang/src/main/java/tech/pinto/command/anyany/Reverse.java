package tech.pinto.command.anyany;

import java.util.function.Supplier;

import tech.pinto.command.Command;
import tech.pinto.command.CommandHelp;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Reverse extends ParameterizedCommand {

	public Reverse(String...args) {
		super("rev", AnyData.class, AnyData.class, args);
		inputCount = args.length == 0 ? Integer.MAX_VALUE :
			Integer.parseInt(args[0]) == -1 ? Integer.MAX_VALUE : Integer.parseInt(args[0]);
		outputCount = inputCount;
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size();
	}

	@Override public Command getReference() {
		return inputStack.removeLast();
	}

	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		// never gets called bc it passes on references to inputs
		throw new UnsupportedOperationException();
	}

	public static Supplier<CommandHelp> getHelp() {
		return () -> new CommandHelp.Builder("rev")
				.inputs("any<sub>1</sub>...any<sub>n</sub>")
				.outputs("any<sub>n</sub>...any<sub>1</sub>")
				.description("Reverses order of *n* stack elements")
				.parameter("n","all",null)
				.build();
	}
}
