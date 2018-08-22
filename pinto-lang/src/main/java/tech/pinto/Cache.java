package tech.pinto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import tech.pinto.Pinto.StackFunction;
import tech.pinto.Pinto.TableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

public class Cache {
	
	private static final long CURRENT_DATA_TIMEOUT = 60 * 1000l;
	
	private static final HashMap<String, RangeMap<Long, CachedSeriesList>> rowCache = new HashMap<>();
	private static final HashMap<String, LinkedList<Column<?>>> columns = new HashMap<>();
	private static final HashMap<String, Integer> columnCounts = new HashMap<>();
	private static final HashMap<String, Function<PeriodicRange<?>,double[][]>> rowFunctions = new HashMap<>();

	public static void clearCache(String key) {
		columns.remove(key);
		columnCounts.remove(key);
		rowFunctions.remove(key);
		for(String per: Periodicities.allCodes()) {
			rowCache.remove(key.concat(":").concat(per));
		}
	}
	
	public static StackFunction cacheNullaryFunction(String key, TableFunction uncached) {
		return (p,s) -> {
			s.addAll(0, getCachedColumns(p, key, uncached));
		};
	}

	private static LinkedList<Column<?>> getCachedColumns(Pinto pinto, String key, TableFunction tableFunction) {
		if(!columns.containsKey(key)) {
			Table t = new Table();
			tableFunction.accept(pinto, t);
			LinkedList<Column<?>> cols = t.flatten();
			columnCounts.put(key, cols.size());
			boolean rowCacheable = true;
			for(int i = 0; i < cols.size(); i++) {
				if(!(cols.get(i) instanceof Column.OfDoubles)) {
					rowCacheable = false;
				}
			}
			if(rowCacheable) {
				LinkedList<Column<?>> cached = new LinkedList<>();
				for(int i = 0; i < cols.size(); i++) {
					cached.add(createRowCachedColumn(key,i, cols.get(i).getHeader(), cols.get(i).getTrace()));
				}
				columns.put(key, cached);
				rowFunctions.put(key, getRowFunctionForColumns(cols));
			} else {
				columns.put(key, cols);
			}
		}
		LinkedList<Column<?>> cols = new LinkedList<>(columns.get(key));
		cols.replaceAll(c -> c.clone());
		return cols;
	}

	private static Column.OfDoubles createRowCachedColumn(String key, int col, String header, String trace) {
		return new Column.OfDoubles(i -> header, i -> trace,
				(range, inputs) -> {
					try {
						return getCachedRows(key, col, range);
					} catch(Throwable t) {
						throw new PintoSyntaxException("Error getting cached values for \"" + key + "\"", t);
					}
				}
		);
	}
	
	private static Function<PeriodicRange<?>,double[][]> getRowFunctionForColumns(LinkedList<Column<?>> l) {
		return range -> {
			double[][] newData = new double[l.size()][];
			for(int i = 0; i < newData.length; i++) {
				newData[i] = Column.castColumn(l.get(i),Column.OfDoubles.class).rows(range); 
			}
			return newData;
		};
	}

	public static void putFunction(String key, int columns, Function<PeriodicRange<?>,double[][]> function) {
		columnCounts.put(key, columns);
		rowFunctions.put(key, function);
	}

	public static <P extends Period<P>> double[] getCachedRows(String key, int col, PeriodicRange<P> range) {
		Periodicity<P> per =  range.periodicity();
		String perKey = key.concat(":").concat(range.periodicity().code());
		RangeMap<Long, CachedSeriesList> cache = null;
		synchronized (rowCache) {
			if (!rowCache.containsKey(perKey)) {
				rowCache.put(perKey, TreeRangeMap.create());
			}
			cache = rowCache.get(perKey);
		}
		synchronized (cache) {
			try {
				Range<Long> requestedRange = Range.closed(range.start().longValue(), range.end().longValue());
				Set<Range<Long>> toRemove = new HashSet<>();
				double[][] chunkData = new double[columnCounts.get(key)][];
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
						concat(chunkData, rowFunctions.get(key).apply(per.range(current, thisChunkStart - 1)));
					}
					concat(chunkData, e.getValue().getSeries());
					current = thisChunkEnd + 1;
				}
				if (current <= requestedRange.upperEndpoint()) {
					concat(chunkData, rowFunctions.get(key).apply(per.range(current, requestedRange.upperEndpoint())));
					current = requestedRange.upperEndpoint() + 1;
				}
				toRemove.stream().forEach(cache::remove);
				long now = per.from(LocalDate.now()).longValue();
				if(now > chunkStart) {
					long endOfPast = Math.min(now - 1, current - 1);
					cache.put(Range.closed(chunkStart, endOfPast),
							new CachedSeriesList(per.range(chunkStart,  endOfPast),
									dup(chunkData,0, (int) (1 + endOfPast - chunkStart)), Optional.empty()));
				}
				if(current - 1 >= now) {
					long startOfNow = Math.max(now, chunkStart);
					cache.put(Range.closed(startOfNow, current - 1),
							new CachedSeriesList(per.range(startOfNow,  current - 1),
									dup(chunkData, (int) (startOfNow - chunkStart), (int) (current - startOfNow)),
									Optional.of(expirationTime.orElse(System.currentTimeMillis() + CURRENT_DATA_TIMEOUT))));
				}
				final long finalStart = chunkStart;
				double[] d = new double[(int)range.size()];
				System.arraycopy(chunkData[col], (int) (requestedRange.lowerEndpoint() - finalStart), d, 0, d.length);
				return d;
			} catch (RuntimeException re) {
				rowCache.remove(perKey);
				throw re;
			}
		}
	}

	private static class CachedSeriesList {

		final private PeriodicRange<?> range;
		final private double[][] series;
		final private Optional<Long> expirationTime;

		public CachedSeriesList(PeriodicRange<?> range, double[][] series, Optional<Long> expirationTime) {
			this.series = series;
			this.range = range;
			this.expirationTime = expirationTime;
		}

		public double[][] getSeries() {
			return series;
		}

		public PeriodicRange<?> getRange() {
			return range;
		}

		public Optional<Long> getExpirationTime() {
			return expirationTime;
		}

	}

	private static void concat(double[][] a, double[][] b) {
		for(int i = 0; i < a.length; i++) {
			if (a[i] == null) {
				a[i] = b[i];
			} else {
				double[] temp = new double[a[i].length + b[i].length];
				System.arraycopy(a[i],0,temp,0,a[i].length);
				System.arraycopy(b[i],0,temp,a[i].length,b[i].length);
				a[i] = temp;
			}
		}
	}

	private static double[][] dup(double[][] original, int start, int length) {
		double[][] dup = new double[original.length][];
		for(int i = 0; i < original.length; i++) {
			dup[i] = new double[length];
			System.arraycopy(original[i], start, dup[i], 0, length);
		}
		return dup;
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
