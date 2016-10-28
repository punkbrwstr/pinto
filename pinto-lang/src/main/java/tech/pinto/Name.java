package tech.pinto;

import tech.pinto.function.ComposableFunctionFactory;
import tech.pinto.function.FunctionHelp;

public class Name {

	final private ComposableFunctionFactory function;
	final private java.util.function.Function<String, FunctionHelp> help;

	public Name(ComposableFunctionFactory function, String helpText) {
		this(function, name -> new FunctionHelp.Builder(name).description(helpText).build());
	}

	public Name(ComposableFunctionFactory function, java.util.function.Function<String, FunctionHelp> help) {
		this.function = function;
		this.help = help;
	}

	public ComposableFunctionFactory getFactory() {
		return function;
	}

	public java.util.function.Function<String, FunctionHelp> getHelpFactory() {
		return help;
	}

}
