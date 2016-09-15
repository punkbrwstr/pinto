package tech.pinto.time;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ContiguousSet;
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
		P start = periodicity.offset(start(), Math.min(offset, 0));
		P end = periodicity.offset(end(), Math.max(offset, 0));
		return periodicity.range(start, end, clearCache);
	}

	public Periodicity<P> periodicity() {
		return periodicity;
	}
	
	public boolean clearCache() {
		return clearCache;
	}
	
	@Override
	public String toString() {
		return Joiner.on(":").join(periodicity.code(),start().toString(),end().toString());
	}
}
