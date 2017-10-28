package tech.pinto.extras.functions.terminal;


import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.functions.HeaderLiteral;

public class Report {
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Defines stack as a name");
	
	public static TerminalFunction getOpener(String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer) {
		return new TerminalFunction(name,namespace,previousFunction,indexer) {

			@Override
			public Table getTable() throws PintoSyntaxException {
				// TODO Auto-generated method stub
				return null;
			}};
	}

}
