package tech.pinto;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.google.common.base.Joiner;

import tech.pinto.function.intermediate.Expression;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class Cache {
	
	private final String DELIMITER = "::";
	protected ReentrantLock statementCacheLock = new ReentrantLock();

	abstract protected Vocabulary getVocabulary();
	abstract protected void addStatement(String key, String statement);
	abstract public boolean isSavedStatement(String key);
	abstract protected String getStatement(String key);
	abstract protected void removeStatement(String key);
	abstract protected void addDependency(String key);
	abstract protected void removeDependency(String key);
	abstract protected SortedSet<String> dependenciesStartingWith(String query);
    abstract public <P extends Period> List<TimeSeries> evaluateCached(String k, int streamCount, PeriodicRange<P> range,
            Function<PeriodicRange<P>,List<TimeSeries>> filler);

	public void save(String code, String statement) {
		statementCacheLock.lock();
		try {
			if(isSavedStatement(code)) {
				for(String dependencyCode : getDependsOn(code)) {
					removeDependency(join(code, "dependsOn", dependencyCode));
					removeDependency(join(dependencyCode, "dependedOnBy", code));
				}
			}
			Expression newStatement = null;
			try {
				newStatement = new Expression(this, getVocabulary(), statement, new LinkedList<>());
			} catch(PintoSyntaxException e) {
				throw new RuntimeException("Unparseable saved query.",e);
			}
			for(String dependencyCode : newStatement.getDependencies()) {
				addDependency(join(code, "dependsOn", dependencyCode));
				addDependency(join(dependencyCode, "dependedOnBy", code));
			}
			addStatement(code, statement);
		} finally {
			statementCacheLock.unlock();
		}
	}

	public void deleteSaved(String code) throws IllegalArgumentException {
		statementCacheLock.lock();
		try {
			if(getDependedOnBy(code).size() != 0) {
				throw new IllegalArgumentException("Cannot delete \"" + code + "\" because other "
						+ "saved commands depend on it.");
			}
			for(String dependencyCode : getDependsOn(code)) {
				removeDependency(join(code, "dependsOn", dependencyCode));
				removeDependency(join(dependencyCode, "dependedOnBy", code));
			}
			removeStatement(code);
		} finally {
			statementCacheLock.unlock();
		}
	}

	public String getSaved(String code) {
		statementCacheLock.lock();
		try {
			return getStatement(code);
		} finally {
			statementCacheLock.unlock();
		}
	}
	
	private SortedSet<String> getDependedOnBy(String code) {
		return dependenciesStartingWith(join(code, "dependedOnBy"));
	}

	private SortedSet<String> getDependsOn(String code) {
		return dependenciesStartingWith(join(code, "dependedsOn"));
	}
	
	private String join(String... parts) {
		return Joiner.on(DELIMITER).join(parts);
	}

}
