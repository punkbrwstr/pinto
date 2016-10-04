package tech.pinto.function.terminal;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;

import java.util.stream.Collectors;

import tech.pinto.Cache;
import tech.pinto.Vocabulary;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;

public class Help extends TerminalFunction {

	
	public Help(Cache cache, Vocabulary vocab, LinkedList<Function> inputs, String[] arguments) {
		super("help", inputs, arguments);
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		if(arguments.length == 0) {
			sb.append("Pinto help").append(crlf);
			sb.append("Built-in commands:").append(crlf);
			sb.append(vocab.getCommandNames().stream().collect(Collectors.joining(", "))).append(crlf);
			sb.append("For extended help type: help(full)").append(crlf);
			sb.append("For help with a specific command type: command help").append(crlf);
		} else if(arguments.length == 1 && arguments[0].equals("full")) {
			sb.append(vocab.getAllCommandHelp().stream().map(FunctionHelp::toTableRowString).collect(Collectors.joining(crlf))).append(crlf);
		}
		message = Optional.of(sb.toString());
	}

	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("help")
				.outputs("none")
				.description("Prints help for proceding commands or prints *help type*.")
				.parameter("help type")
				.build();
	}
	

}
