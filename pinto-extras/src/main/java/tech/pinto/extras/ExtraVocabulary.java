package tech.pinto.extras;

import java.util.HashMap;
import java.util.Map;

import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.command.CommandFactory;
import tech.pinto.extras.command.nonedouble.Bloomberg;

public class ExtraVocabulary implements Vocabulary {
	public Map<String, CommandFactory> commands = new HashMap<>();
	public BloombergClient bc = new BloombergClient();
	
	public ExtraVocabulary() {
		commands.putAll(new StandardVocabulary().getCommandMap());
		commands.put("bbg", (c,a) -> new Bloomberg(bc,c,a));
	}


	@Override
	public Map<String, CommandFactory> getCommandMap() {
		return commands;
	}

}
