package tech.pinto;


import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

public class LocalCache extends Cache {
	
	
	public HashMap<String,String> statementCache = new HashMap<>();
	public TreeSet<String> statementDependencyGraph = new TreeSet<>();

	Vocabulary vocabulary;
	
	@Inject
	public LocalCache(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}

	@Override
	protected Vocabulary getVocabulary() {
		return vocabulary;
	}

	@Override
	protected void addStatement(String key, String statement) {
		statementCache.put(key, statement);
	}

	@Override
	public boolean isSavedStatement(String key) {
		return statementCache.containsKey(key);
	}

	@Override
	protected String getStatement(String key) {
		return statementCache.get(key);
	}

	@Override
	protected void removeStatement(String key) {
		statementCache.remove(key);
	}

	@Override
	protected void addDependency(String key) {
		statementDependencyGraph.add(key);
	}

	@Override
	protected void removeDependency(String key) {
		statementDependencyGraph.remove(key);
	}

	@Override
	protected SortedSet<String> dependenciesStartingWith(String query) {
		String after = statementDependencyGraph.ceiling(query);
		SortedSet<String> matching = null;
			if(after == null) {
				matching = statementDependencyGraph.headSet(query);
			} else {
				matching = statementDependencyGraph.subSet(query, after);
			}
		return matching;
	}

}
