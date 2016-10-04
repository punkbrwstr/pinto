package tech.pinto;


import java.time.LocalDate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.inject.Inject;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicity;

public class LocalCache extends Cache {
	
	
	public HashMap<String,String> statementCache = new HashMap<>();
	public TreeSet<String> statementDependencyGraph = new TreeSet<>();
	public HashMap<String,RangeMap<Long,List<TimeSeries>>> doubleDataCache = new HashMap<>();

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
	
	@Override
    public <P extends Period> List<TimeSeries> evaluateCached(String k, int streamCount, PeriodicRange<P> range,
                Function<PeriodicRange<P>,List<TimeSeries>> filler) {
 
        // unique identifier for function call comprised of function name, and parameters
    	Periodicity<P> freq = range.periodicity();
        String wholeKey = k + ":" + range.periodicity().code();
        RangeMap<Long,List<TimeSeries>> cache = null;
        synchronized(doubleDataCache) {
            if(range.clearCache() || !doubleDataCache.containsKey(wholeKey)) {
                doubleDataCache.put(wholeKey, TreeRangeMap.create());
            }
            cache = doubleDataCache.get(wholeKey);
        }
        synchronized(cache) {
        	Range<Long> requestedRange = Range.closed(range.start().longValue(), range.end().longValue());
    		Set<Range<Long>> toRemove = new HashSet<>();
            List<TimeSeries> chunkData = IntStream.range(0,streamCount).mapToObj( i -> new TimeSeries(null,null,DoubleStream.empty()))
            		.collect(Collectors.toList());
            long current = requestedRange.lowerEndpoint();
            long chunkStart = current;
    		for(Map.Entry<Range<Long>, List<TimeSeries>> e : cache.subRangeMap(requestedRange).asMapOfRanges().entrySet()) {
    			long thisChunkStart = e.getValue().get(0).getRange().start().longValue();
    			long thisChunkEnd = e.getValue().get(0).getRange().end().longValue();
    			chunkStart = Long.min(chunkStart, thisChunkStart);
    			toRemove.add(e.getKey());
    			if(current < thisChunkStart) {
    				concat(chunkData, filler.apply(freq.range(current, thisChunkStart-1, false)));
    			} 
    			concat(chunkData, e.getValue());
    			current = thisChunkEnd + 1;
    		}
    		if(current <= requestedRange.upperEndpoint()) {
    			concat(chunkData, filler.apply(freq.range(current, requestedRange.upperEndpoint(),false)));
    			current = requestedRange.upperEndpoint() + 1;
    		}
   			final long finalStart = chunkStart;
    		toRemove.stream().forEach(cache::remove);
    		final long endToSave = Math.min(current - 1, freq.from(LocalDate.now()).longValue() - 1); // don't save anything from this period
    		if(finalStart <= endToSave) {
    			chunkData.stream().forEach(s -> s.setRange(freq.range(finalStart, endToSave, false)));
    			cache.put(Range.closed(finalStart, endToSave), TimeSeries.dup(chunkData, (int) (endToSave - finalStart + 1)));
    		}

    		chunkData.stream().forEach(s -> s.setRange(range));
    		chunkData.stream().forEach(s -> s.setStream(s.stream()
    				.skip(requestedRange.lowerEndpoint() - finalStart).limit(range.size())));
    		return chunkData;
    	}
    }
    
    private void concat(List<TimeSeries> a, List<TimeSeries> b) {
    	for(int i = 0; i < a.size(); i++) {
    		a.get(i).setStream(DoubleStream.concat(a.get(i).stream(), b.get(i).stream()).sequential());
    		if(a.get(i).getLabel() == null) {
    			a.get(i).setLabel(b.get(i).getLabel());
    		}
    	}
    }



}
