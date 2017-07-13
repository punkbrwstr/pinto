package tech.pinto.function.terminal;


import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.header.HeaderLiteral;

public class Define extends TerminalFunction {

	public Define(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
	}
	
	@Override
	public Table getTable() throws PintoSyntaxException {
		if(!(previousFunction.isPresent() && previousFunction.get() instanceof HeaderLiteral)) {
			throw new PintoSyntaxException("Define requires a string literal for the name.");
		}
		String[] args = ((HeaderLiteral) previousFunction.get()).getValue().split(",");
		ComposableFunction function = previousFunction.get().getPrevious().orElseThrow(
				() -> new PintoSyntaxException("Nothing to define."));
		String desc = function.toExpression().toString();
		function.setIsSubFunction();
		if(args.length > 1) {
			desc += " (" + args[1] + ")";
		}
		namespace.define(args[0], desc, function);
		return createTextColumn("Successfully saved.");
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("none")
				.description("Defines the preceding function as *name*.")
				.parameter("name")
				.parameter("description)", "none", null)
				.build();
	}
}
