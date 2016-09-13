package tech.pinto;

import java.util.Map;
import java.util.Set;

import tech.pinto.command.Command;
import tech.pinto.command.CommandFactory;

public interface Vocabulary {
	
	public Map<String,CommandFactory> getCommandMap();

    default public boolean commandExists(String commandName) {
        return getCommandMap().containsKey(commandName);
    }

    default public Command<?, ?, ?, ?> getCommand(String commandName, Cache cache, String... arguments) {
        return getCommandMap().get(commandName).build(cache, arguments);
    }

    default public Set<String> getCommands() {
        return getCommandMap().keySet();
    }
}
