package tech.pinto;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.google.common.base.Joiner;

import jline.console.completer.Completer;
import tech.pinto.Pinto.TableFunction;

public class Namespace implements Completer {
	private final String DELIMITER = "::";	
	
	protected TreeMap<String,Name> names = new TreeMap<>();
	protected TreeSet<String> dependencyGraph = new TreeSet<>();

	@Inject
	public Namespace(Vocabulary vocabulary) {
		for(Name name : vocabulary.getNames()) {
			names.put(name.toString(), name);
		}
	}
	
	public synchronized boolean contains(String name) {
		return names.containsKey(name);
	}

	public synchronized void define(Pinto pinto, String name, Indexer indexer, String description, Set<String> dependencies,
				Consumer<Table> function) {
		if(names.containsKey(name)) {
			if(names.get(name).builtIn()) {
				throw new IllegalArgumentException("Cannot redefine built-in name " + name + ".");
			}
			clearCache(name);
			for(String dependencyCode : getDependsOn(name)) {
				dependencyGraph.remove(join(name, "dependsOn", dependencyCode));
				dependencyGraph.remove(join(dependencyCode, "dependedOnBy", name));
			}
		}
		for(String dependencyName : dependencies) {
			dependencyGraph.add(join(name, "dependsOn", dependencyName));
			dependencyGraph.add(join(dependencyName, "dependedOnBy", name));
		}
		if(indexer.isNone()) {
			names.put(name, Name.nameBuilder(name,Cache.cacheNullaryFunction(name, function))
					.defined().indexer(Indexer.NONE).description(description).build());
		} else {
			names.put(name, Name.nameBuilder(name, (TableFunction) (p,t) -> {
				function.andThen(t2 -> t2.collapseFunction()).accept(t);
			}).defined().indexer(indexer).description(description).build());
		}
	}

	public synchronized void undefine(String name) throws IllegalArgumentException {
		SortedSet<String> dependedOnBy = getDependedOnBy(name);
		if(dependedOnBy.size() != 0) {
			String d = dependedOnBy.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
			throw new IllegalArgumentException("Cannot delete \"" + name + "\" because other names (" + d
						+ ") depend on it.");
		}
		for(String dependencyCode : getDependsOn(name)) {
			dependencyGraph.remove(join(name, "dependsOn", dependencyCode));
			dependencyGraph.remove(join(dependencyCode, "dependedOnBy", name));
		}
		names.remove(name);
	}
	
    public synchronized Name getName(String functionName) {
        return names.get(functionName);
    }
    
    private synchronized void clearCache(String name) {
		Cache.clearCache(name);
		for(String dependencyCode : getDependedOnBy(name)) {
			clearCache(dependencyCode);
		}
    }
	
	private synchronized SortedSet<String> getDependedOnBy(String code) {
		return dependenciesStartingWith(join(code, "dependedOnBy"));
	}

	private synchronized SortedSet<String> getDependsOn(String code) {
		return dependenciesStartingWith(join(code, "dependsOn"));
	}
	
	private synchronized String join(String... parts) {
		return Joiner.on(DELIMITER).join(parts);
	}

    public synchronized Set<String> getNames() {
        return names.keySet();
    }

	protected synchronized SortedSet<String> dependenciesStartingWith(String query) {
		SortedSet<String> matching = new TreeSet<>();
		for(String key : dependencyGraph.tailSet(query)) {
			if(key.startsWith(query)) {
				matching.add(key.split(DELIMITER)[2]);
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
        	int i = buffer.lastIndexOf(" ");
        	String toComplete = i == -1 ? buffer : buffer.substring(i+1, buffer.length());
        	String stub = i == -1 ? "" : buffer.substring(0, i+1);
        	for(String name : namesStartingWith(toComplete)) {
        		candidates.add(stub.concat(name));
        	}
        }

        return candidates.isEmpty() ? -1 : 0;
    }
	
}
