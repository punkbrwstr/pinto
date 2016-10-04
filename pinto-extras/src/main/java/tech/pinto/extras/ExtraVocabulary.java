package tech.pinto.extras;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.extras.function.supplier.Bloomberg;
import tech.pinto.function.FunctionFactory;
import tech.pinto.function.FunctionHelp;

public class ExtraVocabulary implements Vocabulary {
	public Map<String, FunctionFactory> commands = new HashMap<>();
	public Map<String, Supplier<FunctionHelp>> commandHelp = new HashMap<>();
	public BloombergClient bc = new BloombergClient();
	
	public ExtraVocabulary() {
		commands.putAll(new StandardVocabulary().getCommandMap());
		commands.put("bbg", (c,i,s,a) -> new Bloomberg(bc,c,i,a));

		commandHelp.putAll(new StandardVocabulary().getCommandHelpMap());
	}


	@Override
	public Map<String, FunctionFactory> getCommandMap() {
		return commands;
	}


	@Override
	public Map<String, Supplier<FunctionHelp>> getCommandHelpMap() {
		return commandHelp;
	}

}
