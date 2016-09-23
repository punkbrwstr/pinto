package tech.pinto.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.command.CommandFactory;
import tech.pinto.command.CommandHelp;
import tech.pinto.tests.command.CallCounter;

public class TestVocabulary implements Vocabulary {
	public Map<String, CommandFactory> commands = new HashMap<>();

	
	public TestVocabulary() {
		commands.putAll(new StandardVocabulary().getCommandMap());
		commands.put("counter", (c,a) -> new CallCounter(c,a));
	}


	@Override
	public Map<String, CommandFactory> getCommandMap() {
		return commands;
	}


	@Override
	public Map<String, Supplier<CommandHelp>> getCommandHelpMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
