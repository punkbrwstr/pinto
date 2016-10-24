package tech.pinto;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.base.Joiner;

import jline.console.completer.Completer;
import tech.pinto.function.Function;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.PintoFunctionFactory;

public class Namespace implements Completer {
	private final String DELIMITER = "::";	
	
	protected TreeMap<String,Name> names = new TreeMap<>();
	protected TreeSet<String> dependencyGraph = new TreeSet<>();

	@Inject
	public Namespace(Vocabulary vocabulary) {
		names.putAll(vocabulary.getNameMap());
	}
	
	public synchronized boolean contains(String name) {
		return names.containsKey(name);
	}

	public synchronized void define(String name, String description, Function function) {
		if(names.containsKey(name)) {
			for(String dependencyCode : getDependsOn(name)) {
				dependencyGraph.remove(join(name, "dependsOn", dependencyCode));
				dependencyGraph.remove(join(dependencyCode, "dependedOnBy", name));
			}
		}
		for(String dependencyName : function.getDependencies()) {
			dependencyGraph.add(join(name, "dependsOn", dependencyName));
			dependencyGraph.add(join(dependencyName, "dependedOnBy", name));
		}
		names.put(name, new Name(new PintoFunctionFactory(function), description));
	}

	public synchronized void undefine(String name) throws IllegalArgumentException {
		if(getDependedOnBy(name).size() != 0) {
			throw new IllegalArgumentException("Cannot delete \"" + name + "\" because other "
						+ "saved commands depend on it.");
		}
		for(String dependencyCode : getDependsOn(name)) {
			dependencyGraph.remove(join(name, "dependsOn", dependencyCode));
			dependencyGraph.remove(join(dependencyCode, "dependedOnBy", name));
		}
		names.remove(name);
	}
	
    public synchronized Function getFunction(String functionName, LinkedList<Function> inputs, List<String> saveString, String... arguments) {
        return names.get(functionName).getFactory().build(functionName, this, inputs, saveString, arguments);
    }
	
	private synchronized SortedSet<String> getDependedOnBy(String code) {
		return dependenciesStartingWith(join(code, "dependedOnBy"));
	}

	private synchronized SortedSet<String> getDependsOn(String code) {
		return dependenciesStartingWith(join(code, "dependedsOn"));
	}
	
	private synchronized String join(String... parts) {
		return Joiner.on(DELIMITER).join(parts);
	}

    public synchronized FunctionHelp getHelp(String functionName) {
        return names.get(functionName).getHelpFactory().apply(functionName);
    }

    public synchronized Set<String> getNames() {
        return names.keySet();
    }

    public synchronized List<FunctionHelp> getAllHelp() {
        return names.keySet().stream().map(s -> getHelp(s))
        		.collect(Collectors.toList());
    }
	
	protected synchronized SortedSet<String> dependenciesStartingWith(String query) {
		String after = dependencyGraph.ceiling(query);
		SortedSet<String> matching = null;
			if(after == null) {
				matching = dependencyGraph.headSet(query);
			} else {
				matching = dependencyGraph.subSet(query, after);
			}
		return matching;
	}

	protected synchronized Set<String> namesStartingWith(String query) {
		String after = names.ceilingKey(query);
		SortedMap<String,?> matching = null;
			if(after == null) {
				matching = names.headMap(query);
			} else {
				matching = names.subMap(query,after);
			}
		return matching.keySet();
	}

	@Override
	public synchronized int complete(String buffer, int cursor, List<CharSequence> candidates) {

        if (buffer == null) {
            candidates.addAll(names.keySet());
        } else {
        	candidates.addAll(namesStartingWith(buffer));
        }

        return candidates.isEmpty() ? -1 : 0;
    }
	
}
