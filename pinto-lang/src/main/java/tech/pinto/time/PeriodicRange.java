package tech.pinto.time;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

public final class PeriodicRange<P extends Period> {
	
	private final Periodicity<P> periodicity;
	private final Range<P> range;
	private final boolean clearCache;

	PeriodicRange(Periodicity<P> periodcity, Range<P> range, boolean clearCache) {
		this.periodicity = periodcity;
		this.range = range;
		this.clearCache = clearCache;
	}
	
	public ContiguousSet<P> values() {
		return ContiguousSet.create(range, periodicity);
	}
	
	public List<LocalDate> dates() {
		return values().stream().map(p -> p.endDate()).collect(Collectors.toList());
	}
	
	public long size() {
		return periodicity.distance(range.lowerEndpoint(), range.upperEndpoint()) + 1;
	}
	
	public long indexOf(P p) {
		return periodicity.distance(range.lowerEndpoint(), p);
	}

	public long indexOf(LocalDate d) {
		return periodicity.distance(range.lowerEndpoint(), periodicity.from(d));
	}

	public P start() {
		return range.lowerEndpoint();
	}

	public P end() {
		return range.upperEndpoint();
	}
	
	public PeriodicRange<P> expand(long offset) {
		P start = periodicity.offset(Math.min(offset, 0), start());
		P end = periodicity.offset(Math.max(offset, 0), end());
		return periodicity.range(start, end, clearCache);
	}

	public Periodicity<P> periodicity() {
		return periodicity;
	}
	
	public boolean clearCache() {
		return clearCache;
	}
	
	public Map<String,String> asStringMap() {
		return new ImmutableMap.Builder<String, String>()
				.put("start", start().endDate().toString())
				.put("end", end().endDate().toString())
				.put("freq", periodicity().code()).build();	
	}
	
	@Override
	public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final PeriodicRange<?> other = (PeriodicRange<?>) obj;
        return Objects.equals(this.periodicity, other.periodicity)
            && Objects.equals(this.range, other.range);
	}
	
	@Override
	public String toString() {
		return Joiner.on(":").join(periodicity.code(),start().toString(),end().toString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(periodicity.code(),range.hashCode());
	}
}
