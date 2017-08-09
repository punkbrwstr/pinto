package tech.pinto;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.base.Joiner;

import jline.console.completer.Completer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.DefinedFunctionFactory;

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

	public synchronized void define(String name, String description, ComposableFunction function) {
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
		names.put(name, new Name(new DefinedFunctionFactory(function), new FunctionHelp.Builder()
				.description(description)));
	}

	public synchronized void undefine(String name) throws IllegalArgumentException {
		SortedSet<String> dependedOnBy = getDependedOnBy(name);
		if(dependedOnBy.size() != 0) {
			String d = dependedOnBy.stream().map(s -> s.split(DELIMITER)[2])
					.map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
			throw new IllegalArgumentException("Cannot delete \"" + name + "\" because other names (" + d
						+ ") depend on it.");
		}
		for(String dependencyCode : getDependsOn(name)) {
			dependencyGraph.remove(join(name, "dependsOn", dependencyCode));
			dependencyGraph.remove(join(dependencyCode, "dependedOnBy", name));
		}
		names.remove(name);
	}
	
    public synchronized ComposableFunction getFunction(String functionName, Pinto pinto, ComposableFunction previous,
    		Indexer indexer) {
        return names.get(functionName).getFactory().build(functionName, pinto, this, previous, indexer);
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
        return names.get(functionName).getHelpBuilder().build(functionName);
    }

    public synchronized Set<String> getNames() {
        return names.keySet();
    }

    public synchronized List<FunctionHelp> getAllHelp() {
        return names.keySet().stream().map(s -> getHelp(s))
        		.collect(Collectors.toList());
    }
	
	protected synchronized SortedSet<String> dependenciesStartingWith(String query) {
		SortedSet<String> matching = new TreeSet<>();
		for(String key : dependencyGraph.tailSet(query)) {
			if(key.startsWith(query)) {
				matching.add(key);
			} else {
				break;
			}
		}
		return matching;
	}

	protected synchronized Set<String> namesStartingWith(String query) {
		SortedSet<String> matching = new TreeSet<>();
		for(Entry<String, Name> e : names.tailMap(query).entrySet()) {
			if(e.getKey().startsWith(query)) {
				matching.add(e.getKey());
			} else {
				break;
			}
		}
		return matching;
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
