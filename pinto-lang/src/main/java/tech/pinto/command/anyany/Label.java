package tech.pinto.command.anyany;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.function.Supplier;

import tech.pinto.command.Command;
import tech.pinto.command.CommandHelp;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.command.SimpleCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Label extends ParameterizedCommand {
	
	private final ArrayDeque<String> labels = new ArrayDeque<>();

	public Label(String[] arguments) {
		super("label", AnyData.class, AnyData.class, arguments);
		inputCount = arguments.length;
		outputCount = inputCount;
		labels.addAll(Arrays.asList(arguments));
	}
	
	@Override public Command getReference() {
		Command c = inputStack.removeFirst();
		String label = labels.removeFirst();
		return new SimpleCommand(c,1,1,range -> {
				Data<?> data = c.evaluate(range);
				data.setLabel(label);
				return data;
		});
	}

	@Override
	public <P extends Period> AnyData evaluate(PeriodicRange<P> range) {
		// never gets called bc it passes on references to inputs
		throw new UnsupportedOperationException();
	}
	
	public static Supplier<CommandHelp> getHelp() {
		return () -> new CommandHelp.Builder("label")
				.inputs("any<sub>1</sub>...any<sub>z</sub>")
				.outputs("any<sub>1</sub>...any<sub>z</sub>")
				.description("Sets arguments as labels for inputs")
				.parameter("label<sub>1</sub>")
				.parameter("label<sub>z</sub>")
				.build();
	}
	

}
