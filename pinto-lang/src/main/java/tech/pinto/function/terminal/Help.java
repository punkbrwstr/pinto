package tech.pinto.function.terminal;

import java.util.LinkedList;


import java.util.stream.Collectors;

import tech.pinto.ColumnValues;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;

public class Help extends TerminalFunction {

	public Help(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, namespace, previousFunction, indexer, args);
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("none")
				.description("Prints help for proceding commands or prints *help type*.")
				.parameter("help type")
				.build();
	}

	@Override
	public LinkedList<ColumnValues> getColumnValues() throws PintoSyntaxException {
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		if(args.length == 1 && args[0].equals("full")) {
			sb.append(namespace.getAllHelp().stream().map(FunctionHelp::toTableRowString).collect(Collectors.joining(crlf))).append(crlf);
		} else if(args.length == 1) {
			String functionName = args[0].trim();
			if(namespace.contains(functionName)) {
				sb.append(namespace.getHelp(functionName).toConsoleHelpString());
			} else {
				sb.append("Function " + functionName + " not found.");
			}
		} else {
			sb.append("Pinto help").append(crlf);
			sb.append("Built-in commands:").append(crlf);
			sb.append(namespace.getNames().stream().collect(Collectors.joining(", "))).append(crlf);
			sb.append("For extended description of functions type: help(full)").append(crlf);
			sb.append("For help with a specific function type: help(<function>)").append(crlf);
			
		}
		return createTextColumn(sb.toString());
	}
	

}
