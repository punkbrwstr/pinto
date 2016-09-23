package tech.pinto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import tech.pinto.command.Command;
import tech.pinto.command.CommandFactory;
import tech.pinto.command.CommandHelp;

public interface Vocabulary {
	
	public Map<String,CommandFactory> getCommandMap();

	public Map<String,Supplier<CommandHelp>> getCommandHelpMap();

    default public boolean commandExists(String commandName) {
        return getCommandMap().containsKey(commandName);
    }

    default public Command getCommand(String commandName, Cache cache, String... arguments) {
        return getCommandMap().get(commandName).build(cache, arguments);
    }

    default public CommandHelp getHelp(String commandName) {
        return getCommandHelpMap().get(commandName).get();
    }

    default public Set<String> getCommandNames() {
        return getCommandMap().keySet();
    }

    default public Collection<CommandHelp> getAllCommandHelp() {
        return getCommandHelpMap().values().stream().map(s -> s.get())
        		.collect(Collectors.toList());
    }
}
