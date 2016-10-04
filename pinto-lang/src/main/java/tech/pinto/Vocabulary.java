package tech.pinto;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import tech.pinto.function.FunctionFactory;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;

public interface Vocabulary {
	
	public Map<String,FunctionFactory> getCommandMap();

	public Map<String,Supplier<FunctionHelp>> getCommandHelpMap();

    default public boolean commandExists(String commandName) {
        return getCommandMap().containsKey(commandName);
    }

    default public Function getCommand(String commandName, Cache cache,
    		LinkedList<Function> inputs, List<String> saveString, String... arguments) {
        return getCommandMap().get(commandName).build(cache, inputs, saveString, arguments);
    }

    default public FunctionHelp getHelp(String commandName) {
        return getCommandHelpMap().get(commandName).get();
    }

    default public Set<String> getCommandNames() {
        return getCommandMap().keySet();
    }

    default public Collection<FunctionHelp> getAllCommandHelp() {
        return getCommandHelpMap().values().stream().map(s -> s.get())
        		.collect(Collectors.toList());
    }
}
