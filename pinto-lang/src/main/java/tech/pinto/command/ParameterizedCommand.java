package tech.pinto.command;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.data.Data;

abstract public class ParameterizedCommand extends Command {
	
	protected final String[] arguments;

	public ParameterizedCommand(String name, Class<? extends Data<?>> inputType,
			Class<? extends Data<?>> outputType, String...arguments) {
		super(name, inputType, outputType);
		this.arguments = arguments;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if(arguments.length > 0) {
			sb.append(Stream.of(arguments).collect(Collectors.joining(",", "(", ")")));
		}
		return sb.toString();
	}

}
