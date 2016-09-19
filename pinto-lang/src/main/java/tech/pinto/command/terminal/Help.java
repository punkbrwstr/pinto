package tech.pinto.command.terminal;

import java.util.stream.Collectors;

import tech.pinto.Cache;
import tech.pinto.Vocabulary;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.MessageData;
import tech.pinto.data.NoneData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Help extends ParameterizedCommand {

	private final String text;
	
	public Help(Cache cache, Vocabulary vocab, String[] arguments) {
		super("help", NoneData.class, MessageData.class, arguments);
		inputCount = 0;
		outputCount = 1;
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		if(arguments.length == 0) {
			sb.append("Pinto help").append(crlf);
			sb.append("Built-in commands:").append(crlf);
			sb.append(vocab.getCommands().stream().collect(Collectors.joining(", "))).append(crlf);
			sb.append("For help with a specific command type: help(command)").append(crlf);
		} else if(arguments.length == 1) {
			sb.append("help for ").append(arguments[0]).append(crlf);
			sb.append("here is the help").append(crlf);
		}
		text = sb.toString();
	}

	@Override
	public <P extends Period> MessageData evaluate(PeriodicRange<P> range) {
		return new MessageData(text);
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	
	

}
