package tech.pinto;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.google.common.base.Joiner;

import tech.pinto.command.anyany.Statement;
import tech.pinto.data.DoubleData;
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
    abstract public <P extends Period> List<DoubleData> evaluateCached(String k, int streamCount, PeriodicRange<P> range,
            Function<PeriodicRange<P>,List<DoubleData>> filler);

	public void save(String code, String statement) {
		statementCacheLock.lock();
		try {
			if(isSavedStatement(code)) {
				Statement oldStatement = null;
				try {
					oldStatement = new Statement(this, getVocabulary(), getSaved(code),true);
				} catch(PintoSyntaxException e) {
					throw new RuntimeException("Unparseable saved query.",e);
				}
				for(String dependencyCode : oldStatement.getDependencies()) {
					removeDependency(join(code, "dependsOn", dependencyCode));
					removeDependency(join(dependencyCode, "dependedOnBy", code));
				}
			}
			Statement newStatement = null;
			try {
				newStatement = new Statement(this, getVocabulary(), statement, true);
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
			if(getDependencies(code).size() != 0) {
				throw new IllegalArgumentException("Cannot delete \"" + code + "\" because other "
						+ "saved commands depend on it.");
			}
			Statement oldStatement;
			try {
				oldStatement = new Statement(this, getVocabulary(), getSaved(code), true);
			} catch (PintoSyntaxException e) {
				throw new RuntimeException();
			}
			for(String dependencyCode : oldStatement.getDependencies()) {
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
	
	private SortedSet<String> getDependencies(String code) {
		return dependenciesStartingWith(join(code, "dependedOnBy"));
	}
	
	private String join(String... parts) {
		return Joiner.on(DELIMITER).join(parts);
	}

}
