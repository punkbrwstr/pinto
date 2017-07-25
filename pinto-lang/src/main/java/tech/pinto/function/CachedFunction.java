package tech.pinto.function;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import tech.pinto.Indexer;
import tech.pinto.Column;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicity;

abstract public class CachedFunction extends ComposableFunction {
	
	public static final long CURRENT_DATA_TIMEOUT = 60 * 1000l;
	
	public static HashMap<String, RangeMap<Long, CachedSeriesList>> seriesCache = new HashMap<>();
	public static HashMap<String, List<String>> textCache = new HashMap<>();

	public CachedFunction(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	public CachedFunction(String name, ComposableFunction previousFunction, Indexer indexer, ParameterType parameterType) {
		super(name, previousFunction, indexer, parameterType);
	}

	abstract protected <P extends Period> List<DoubleStream> getUncachedSeries(PeriodicRange<P> range);

	abstract protected List<String> getUncachedText();

	abstract protected int columns();

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		for (int i = 0; i < columns(); i++) {
			final int index = i;
			stack.addFirst(new Column(inputs -> getCachedText().get(index),
					inputs -> range -> this.getCachedValues(range).get(index)));
		}
		return stack;
	}

	private String getKey() {
		return toString();
	}

	private List<String> getCachedText() {
		synchronized (textCache) {
			if (!textCache.containsKey(getKey())) {
				textCache.put(getKey(), getUncachedText());
			}
			return textCache.get(getKey());
		}
	}

	private <P extends Period> List<DoubleStream> getCachedValues(PeriodicRange<P> range) {
		Periodicity<P> freq = range.periodicity();
		String wholeKey = getKey() + ":" + range.periodicity().code();
		RangeMap<Long, CachedSeriesList> cache = null;
		synchronized (seriesCache) {
			if (range.clearCache() || !seriesCache.containsKey(wholeKey)) {
				seriesCache.put(wholeKey, TreeRangeMap.create());
			}
			cache = seriesCache.get(wholeKey);
		}
		synchronized (cache) {
			try {
				Range<Long> requestedRange = Range.closed(range.start().longValue(), range.end().longValue());
				Set<Range<Long>> toRemove = new HashSet<>();
				List<DoubleStream> chunkData = new ArrayList<>();
				long current = requestedRange.lowerEndpoint();
				long chunkStart = current;
				Optional<Long> expirationTime = Optional.empty();
				for (Map.Entry<Range<Long>, CachedSeriesList> e : cache.subRangeMap(requestedRange).asMapOfRanges()
						.entrySet()) {
					toRemove.add(e.getKey());
					if(e.getValue().getExpirationTime().isPresent()) {
						if(System.currentTimeMillis() > e.getValue().getExpirationTime().get()) {
							break;
						} else {
							expirationTime = e.getValue().getExpirationTime();
						}
					}
					long thisChunkStart = e.getValue().getRange().start().longValue();
					long thisChunkEnd = e.getValue().getRange().end().longValue();
					chunkStart = Long.min(chunkStart, thisChunkStart);
					if (current < thisChunkStart) {
						concat(chunkData, getUncachedSeries(freq.range(current, thisChunkStart - 1, false)));
					}
					concat(chunkData, e.getValue().getSeries());
					current = thisChunkEnd + 1;
				}
				if (current <= requestedRange.upperEndpoint()) {
					concat(chunkData, getUncachedSeries(freq.range(current, requestedRange.upperEndpoint(), false)));
					current = requestedRange.upperEndpoint() + 1;
				}
				toRemove.stream().forEach(cache::remove);
				long now = freq.from(LocalDate.now()).longValue();
				if(now > chunkStart) {
					long endOfPast = Math.min(now - 1, current - 1);
					cache.put(Range.closed(chunkStart, endOfPast),
							new CachedSeriesList(freq.range(chunkStart,  endOfPast, false),
									dup(chunkData,0, (int) (1 + endOfPast - chunkStart)), Optional.empty()));
				}
				if(current - 1 >= now) {
					long startOfNow = Math.max(now, chunkStart);
					cache.put(Range.closed(startOfNow, current - 1),
							new CachedSeriesList(freq.range(startOfNow,  current - 1, false),
									dup(chunkData, (int) (startOfNow - chunkStart), (int) (current - startOfNow)),
									Optional.of(expirationTime.orElse(System.currentTimeMillis() + CURRENT_DATA_TIMEOUT))));
				}
				final long finalStart = chunkStart;
				return chunkData.stream()
						.map(s -> s.skip(requestedRange.lowerEndpoint() - finalStart).limit(range.size()))
						.collect(Collectors.toList());
			} catch (RuntimeException re) {
				seriesCache.remove(wholeKey);
				throw re;
			}
		}
	}

	private static class CachedSeriesList {

		final private PeriodicRange<?> range;
		final private List<DoubleStream> series;
		final private Optional<Long> expirationTime;

		public CachedSeriesList(PeriodicRange<?> range, List<DoubleStream> series, Optional<Long> expirationTime) {
			this.series = series;
			this.range = range;
			this.expirationTime = expirationTime;
		}

		public List<DoubleStream> getSeries() {
			return series;
		}

		public PeriodicRange<?> getRange() {
			return range;
		}

		public Optional<Long> getExpirationTime() {
			return expirationTime;
		}

	}

	private static void concat(List<DoubleStream> a, List<DoubleStream> b) {
		if (a.size() == 0) {
			a.addAll(b);
		} else {
			List<DoubleStream> temp = new ArrayList<>(a);
			a.clear();
			for (int i = 0; i < temp.size(); i++) {
				a.add(DoubleStream.concat(temp.get(i), b.get(i)).sequential());
			}
		}
	}

	private static List<DoubleStream> dup(List<DoubleStream> original, int start, int length) {
		List<DoubleStream> temp = new ArrayList<>(original);
		List<DoubleStream> copy = new ArrayList<>();
		original.clear();
		for (DoubleStream s : temp) {
			double[] o = s.toArray();
			double[] c = new double[length];
			System.arraycopy(o, start, c, 0, length);
			original.add(DoubleStream.of(o));
			copy.add(DoubleStream.of(c));
		}
		return copy;
	}
	
//	public static void main(String[] s) {
//		Consumer<DoubleStream> r1 = original1 -> {
//			DoubleStream.Builder b1 = DoubleStream.builder();
//			DoubleStream.Builder b2 = DoubleStream.builder();
//			DoubleStream.Builder b3 = DoubleStream.builder();
//			original1.peek(b1::accept).peek(b2::accept).forEach(b3::accept);
//			b1.build();
//			b2.build();
//			b3.build();
//		};
//		Consumer<DoubleStream> r2 = original2 -> {
//			double[] a1 = original2.toArray();
//			double[] a2 = new double[a1.length];
//			double[] a3 = new double[a1.length];
//			System.arraycopy(a1,0,a2,0,a1.length);
//			System.arraycopy(a1,0,a3,0,a1.length);
//			DoubleStream.of(a1);
//			DoubleStream.of(a2);
//			DoubleStream.of(a3);
//		};
//
//		
//		System.out.println(LongStream.generate(() -> timeme(r1)).limit(20).average().getAsDouble());
//		System.out.println(LongStream.generate(() -> timeme(r2)).limit(20).average().getAsDouble());
//	}
//	
//	public static long timeme(Consumer<DoubleStream> r) {
//		AtomicLong al = new AtomicLong();
//		final DoubleStream original = DoubleStream.generate(() -> (double) al.getAndIncrement()).limit(10000000);
//		long start = System.nanoTime();
//		r.accept(original);
//		return System.nanoTime() - start;
//	}

}
