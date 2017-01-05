package tech.pinto.function.supplier;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import tech.pinto.Indexer;
import tech.pinto.TimeSeries;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicity;

abstract public class CachedSupplierFunction extends ComposableFunction {

	public static HashMap<String,RangeMap<Long,List<TimeSeries>>> doubleDataCache = new HashMap<>();

	public CachedSupplierFunction(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	abstract protected <P extends Period> List<DoubleStream> evaluateAll(PeriodicRange<P> range);
	abstract protected List<String> allLabels();
	abstract protected int additionalOutputCount();

	protected <P extends Period> DoubleStream evaluateOne(int i, PeriodicRange<P> range) {
		return evaluateCached(toString(), additionalOutputCount(), range, r -> evaluateAll(r)).get(i);
	}
	
	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		for(int i = 0; i < additionalOutputCount(); i++) {
			final int index = i;
			stack.addFirst(new EvaluableFunction(f -> allLabels().get(index),inputs -> range -> this.evaluateOne(index, range)));
		}
		return stack;
	}

    public <P extends Period> List<DoubleStream> evaluateCached(String k, int streamCount, PeriodicRange<P> range,
                java.util.function.Function<PeriodicRange<P>,List<DoubleStream>> f) {
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
		java.util.function.Function<PeriodicRange<P>,List<TimeSeries>> filler = 
				r -> {
					try {
						return f.apply(r).stream().map(ds -> new TimeSeries(null,null,ds)).collect(Collectors.toList());
					} catch(RuntimeException re) {
						synchronized(doubleDataCache) {
							doubleDataCache.remove(wholeKey);
						}
						throw re;
					}
				};
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
    		return chunkData.stream().map(ts -> ts.stream()).collect(Collectors.toList());
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
