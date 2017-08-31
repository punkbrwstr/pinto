package tech.pinto.function.functions.terminal;

import java.util.Optional;
import java.util.stream.Collectors;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Parameters;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.functions.HeaderLiteral;

public class Help extends TerminalFunction {
	 
	private static final Parameters.Builder PARAMETER_BUILDER = new Parameters.Builder()
			.add("type", false, "Pinto name for which you want help.");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETER_BUILDER.build())
			.description("Prints help for proceding commands or prints *help type*.");

	public Help(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
	}

	@Override
	public Table getTable() throws PintoSyntaxException {
		Optional<String> argument = Optional.empty();
		if(previousFunction.isPresent() && previousFunction.get() instanceof HeaderLiteral) {
			argument = Optional.of(((HeaderLiteral) previousFunction.get()).getValue());
		}
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		if(argument.isPresent() && argument.get().equals("full")) {
			sb.append(namespace.getAllHelp().stream().map(FunctionHelp::toTableRowString).collect(Collectors.joining(crlf))).append(crlf);
		} else if(argument.isPresent()) {
			String functionName = argument.get().trim();
			if(namespace.contains(functionName)) {
				sb.append(namespace.getHelp(functionName).toConsoleHelpString());
			} else {
				sb.append("Function " + functionName + " not found.");
			}
		} else {
			sb.append("Pinto help").append(crlf);
			sb.append("Function names:").append(crlf);
			sb.append(namespace.getNames().stream().collect(Collectors.joining(", "))).append(crlf);
			sb.append("For extended description of functions type: \"full\" help").append(crlf);
			sb.append("For help with a specific function type: \"function name\" help").append(crlf);
			
		}
		return createTextColumn(sb.toString());
	}
}
