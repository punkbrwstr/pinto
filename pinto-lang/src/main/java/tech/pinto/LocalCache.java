package tech.pinto;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import javax.inject.Inject;

import com.google.common.base.Joiner;

import tech.pinto.command.anyany.Statement;
import tech.pinto.command.nonedouble.CachedDoubleCommand;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class LocalCache extends Cache {
	
	private final String DELIMITER = ":";
	
	public HashMap<String,String> statementCache = new HashMap<>();
	public TreeSet<String> statementDependencyGraph = new TreeSet<>();
	public HashMap<String,TreeMap<Long,double[]>> doubleDataCache = new HashMap<>();

	Vocabulary vocabulary;
	
	@Inject
	public LocalCache(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}

	@Override
	public <P extends Period> DoubleStream evaluateCached(
			CachedDoubleCommand command, PeriodicRange<P> range,
				Function<PeriodicRange<P>,DoubleStream> function) {

		// unique identifier for function call comprised of function name, and parameters
		String key = join(command.toString(), range.periodicity().code());
		TreeMap<Long,double[]> cache = null;
		synchronized(doubleDataCache) {
			if(!doubleDataCache.containsKey(key)) {
				doubleDataCache.put(key, new TreeMap<>());
			}
			cache = doubleDataCache.get(key);
		}
		synchronized(cache) {
			DoubleStream data = DoubleStream.empty();
			P startOfData = range.start(), endOfData = range.end(), current = range.start(); // current is the first day we still need
			boolean filledData = false;
			// loop through chunks.  "e" chunks get filtered out by redis.
			for(Map.Entry<Long,double[]> chunk : cache.headMap(range.end().longValue(), true).entrySet()) {
				P chunkStart = range.periodicity().get(chunk.getKey());
				P chunkEnd = range.periodicity().offset(chunkStart, chunk.getValue().length -1);
					
				if(chunkEnd.isBefore(current)) {
					continue; // ignore "a" chunks
				} 
				startOfData = chunkStart.isBefore(startOfData) ? chunkStart : startOfData;
				endOfData = chunkEnd.isAfter(endOfData) ? chunkEnd : endOfData;
				if(chunkStart.isAfter(current)) { // need to fill space before chunk
					filledData = true;
					DoubleStream neededData = function.apply(range.periodicity()
								.range(current,range.periodicity().previous(chunkStart),false));
				//System.out.println("appending missing data from formula:");
				//neededData = peek(neededData);
					data = DoubleStream.concat(data, neededData).sequential();
					current = chunkStart;
				}
				data = DoubleStream.concat(data, DoubleStream.of(chunk.getValue())).sequential();
				current = range.periodicity().next(chunkEnd);
				//System.out.println("appended data from redis:");
				//data = peek(data);
			}
			
			if(!current.isAfter(range.end())) { // fill in more until the end if we need it
				filledData = true;
				DoubleStream neededData = function.apply(range.periodicity()
								.range(current,range.end(),false));
				//System.out.println("appending missing data from formula:");
				//neededData = peek(neededData);
				data = DoubleStream.concat(data, neededData).sequential();
			}

			if(filledData) { // if it's a "f" we don't have to save
				DoubleStream.Builder b = DoubleStream.builder();
				long lengthToSave = range.periodicity().distance(startOfData, endOfData) + 1; 
				P now = range.periodicity().from(LocalDate.now());
				// don't save anything after yesterday
				if(!now.isAfter(endOfData)) {
					lengthToSave -= (range.periodicity().distance(now, endOfData) + 1);
				}
				DoubleStream.Builder bToSave = DoubleStream.builder();
				data.peek(bToSave::accept).forEachOrdered(b::accept); // trick to duplicate streams
				
				if(lengthToSave > 0) {
					cache.subMap(startOfData.longValue(), true, endOfData.longValue(), true).clear();
					cache.put(startOfData.longValue(), bToSave.build().limit(lengthToSave).toArray());
				}
				data = b.build();
			}
			return data.skip(range.periodicity().distance(startOfData, range.start()))
							.limit(range.size());
		} 
	}

	@Override
	public void save(String code, String statement) {
		synchronized(statementCache) {
			if(isSaved(code)) {
				Statement oldStatement = null;
				try {
					oldStatement = new Statement(this, vocabulary, getSaved(code));
				} catch(PintoSyntaxException e) {
					throw new RuntimeException("Unparseable saved query.",e);
				}
				for(String dependencyCode : oldStatement.getDependencies()) {
					statementDependencyGraph.remove(join(code, "dependsOn", dependencyCode));
					statementDependencyGraph.remove(join(dependencyCode, "dependedOnBy", code));
				}
			}
			Statement newStatement = null;
			try {
				newStatement = new Statement(this, vocabulary, statement);
			} catch(PintoSyntaxException e) {
				throw new RuntimeException("Unparseable saved query.",e);
			}
			for(String dependencyCode : newStatement.getDependencies()) {
				statementDependencyGraph.add(join(code, "dependsOn", dependencyCode));
				statementDependencyGraph.add(join(dependencyCode, "dependedOnBy", code));
			}
			statementCache.put(code, statement);
		}
	}

	@Override
	public String deleteSaved(String code) throws IllegalArgumentException {
		synchronized(statementCache) {
			if(getDependencies(code).size() != 0) {
				throw new IllegalArgumentException("Cannot delete \"" + code + "\" because other "
						+ "saved commands depend on it.");
			}
			Statement oldStatement;
			try {
				oldStatement = new Statement(this, vocabulary, getSaved(code));
			} catch (PintoSyntaxException e) {
				throw new RuntimeException();
			}
			for(String dependencyCode : oldStatement.getDependencies()) {
				statementDependencyGraph.remove(join(code, "dependsOn", dependencyCode));
				statementDependencyGraph.remove(join(dependencyCode, "dependedOnBy", code));
			}
			return statementCache.remove(code);
		}
	}

	@Override
	public boolean isSaved(String code) {
		synchronized(statementCache) {
			return statementCache.containsKey(code);
		}
	}

	@Override
	public String getSaved(String code) {
		synchronized(statementCache) {
			return statementCache.get(code);
		}
	}
	
	private SortedSet<String> getDependencies(String code) {
		String query = join(code, "dependedOnBy");
		String after = statementDependencyGraph.ceiling(query);
		SortedSet<String> matching = null;
			if(after == null) {
				matching = statementDependencyGraph.headSet(query);
			} else {
				matching = statementDependencyGraph.subSet(query, after);
			}
		return matching;
	}
	
	private String join(String... parts) {
		return Joiner.on(DELIMITER).join(parts);
	}

}
