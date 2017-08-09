package tech.pinto;

import tech.pinto.function.FunctionFactory;
import tech.pinto.function.FunctionHelp;

public class Name {

	final private FunctionFactory function;
	final private FunctionHelp.Builder help;


	public Name(FunctionFactory function, FunctionHelp.Builder help) {
		this.function = function;
		this.help = help;
	}

	public FunctionFactory getFactory() {
		return function;
	}
	
	public FunctionHelp.Builder getHelpBuilder() {
		return help;
	}

}
