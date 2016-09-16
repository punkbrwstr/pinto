package tech.pinto.command.terminal;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

import tech.pinto.Cache;
import tech.pinto.Vocabulary;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.MessageData;
import tech.pinto.data.NoneData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Help extends ParameterizedCommand<Object, NoneData, String, MessageData> {

	private final Vocabulary vocab;

	public Help(Cache cache, Vocabulary vocab, String[] arguments) {
		super("help", NoneData.class, MessageData.class, arguments);
		this.vocab = vocab;
	}
	

	@Override
	protected <P extends Period> ArrayDeque<MessageData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<MessageData> out = new ArrayDeque<>();
		if(arguments.length == 0) {
			out.addLast(new MessageData("Pinto help"));
			out.addLast(new MessageData("Built-in commands:"));
			out.addLast(new MessageData(
					vocab.getCommands().stream().collect(Collectors.joining(", "))));
			out.addLast(new MessageData("For help with a specific command type: help(command)"));
			
		} else if(arguments.length == 1) {
			out.addFirst(new MessageData("help for " + arguments[0]));
			out.addFirst(new MessageData("here is the help"));
		}

		return out;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	
	

}
