package tech.pinto.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.function.FunctionFactory;
import tech.pinto.function.FunctionHelp;
import tech.pinto.tests.command.CallCounter;

public class TestVocabulary implements Vocabulary {
	public Map<String, FunctionFactory> commands = new HashMap<>();

	
	public TestVocabulary() {
		commands.putAll(new StandardVocabulary().getCommandMap());
		commands.put("counter", (c,i,s,a) -> new CallCounter(c,i,a));
	}


	@Override
	public Map<String, FunctionFactory> getCommandMap() {
		return commands;
	}


	@Override
	public Map<String, Supplier<FunctionHelp>> getCommandHelpMap() {
		return null;
	}

}
