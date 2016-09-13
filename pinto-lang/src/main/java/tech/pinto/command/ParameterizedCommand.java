package tech.pinto.command;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.data.Data;

abstract public class ParameterizedCommand<IT,ID extends Data<IT>,OT, OD extends Data<OT>> 
			extends Command<IT,ID,OT,OD> {
	
	protected final String[] arguments;

	public ParameterizedCommand(String name, Class<ID> inputType, Class<OD> outputType, String...arguments) {
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
