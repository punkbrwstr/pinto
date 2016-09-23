package tech.pinto.command.terminal;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import tech.pinto.Cache;
import tech.pinto.Vocabulary;
import tech.pinto.command.Command;
import tech.pinto.command.CommandHelp;
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
			sb.append(vocab.getCommandNames().stream().collect(Collectors.joining(", "))).append(crlf);
			sb.append("For extended help type: help(full)").append(crlf);
			sb.append("For help with a specific command type: command help").append(crlf);
		} else if(arguments.length == 1 && arguments[0].equals("full")) {
			sb.append(vocab.getAllCommandHelp().stream().map(CommandHelp::toTableRowString).collect(Collectors.joining(crlf))).append(crlf);
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
	
	public static Supplier<CommandHelp> getHelp() {
		return () -> new CommandHelp.Builder("help")
				.inputs("any<sub>1</sub>...any<sub>n</sub>")
				.outputs("none")
				.description("Prints help for proceding commands or prints *help type*.")
				.parameter("help type")
				.build();
	}
	

}
