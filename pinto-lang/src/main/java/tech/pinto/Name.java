package tech.pinto;

import tech.pinto.function.FunctionFactory;
import tech.pinto.function.FunctionHelp;

public class Name {

	final private FunctionFactory function;
	final private java.util.function.Function<String, FunctionHelp> help;

	public Name(FunctionFactory function, String helpText) {
		this(function, name -> new FunctionHelp.Builder(name).description(helpText).build());
	}

	public Name(FunctionFactory function, java.util.function.Function<String, FunctionHelp> help) {
		this.function = function;
		this.help = help;
	}

	public FunctionFactory getFactory() {
		return function;
	}

	public java.util.function.Function<String, FunctionHelp> getHelpFactory() {
		return help;
	}

}
