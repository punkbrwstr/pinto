package pinto.tests.command;

import java.util.HashMap;
import java.util.Map;

import pinto.PintoVocabulary;
import pinto.Vocabulary;
import pinto.command.CommandFactory;

public class TestVocabulary implements Vocabulary {
	public Map<String, CommandFactory> commands = new HashMap<>();

	
	public TestVocabulary() {
		commands.putAll(new PintoVocabulary().getCommandMap());
		commands.put("counter", (c,a) -> new CallCounter(c,a));
	}


	@Override
	public Map<String, CommandFactory> getCommandMap() {
		return commands;
	}

}
