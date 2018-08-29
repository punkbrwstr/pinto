package tech.pinto.time;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import tech.pinto.tools.ID;

public final class PeriodicRange<P extends Period<P>> {
	
	private final Periodicity<P> periodicity;
	private final P startInclusive;
	private final P endInclusive;
	private final String id = ID.getId();

	PeriodicRange(Periodicity<P> periodcity, P startInclusive, P endInclusive) {
		if(endInclusive.isBefore(startInclusive)) {
			throw new IllegalArgumentException("Range end must be >= range start");
		}
		this.periodicity = periodcity;
		this.startInclusive = startInclusive;
		this.endInclusive = endInclusive;
	}
	
	public String getId() {
		return id;
	}
	
	public List<P> values() {
		List<P> l = new ArrayList<>();
		P p = start();
		do {
			l.add(p);
			p = (P) p.next();
		} while(!p.isAfter(end()));
		return l;
	}
	
	public List<LocalDate> dates() {
		List<LocalDate> l = new ArrayList<>();
		P p = start();
		do {
			l.add(p.endDate());
			p = p.next();
		} while(!p.isAfter(end()));
		return l;
	}
	
	public long size() {
		return periodicity.distance(startInclusive, endInclusive) + 1;
	}
	
	public long indexOf(P period) {
		return periodicity.distance(startInclusive, (P) period);
	}

	public long indexOf(LocalDate d) {
		return periodicity.distance(startInclusive, periodicity.from(d));
	}

	public P start() {
		return startInclusive;
	}

	public P end() {
		return endInclusive;
	}
	
	public PeriodicRange<P> expand(long offset) {
		P start = periodicity.offset(Math.min(offset, 0), start());
		P end = periodicity.offset(Math.max(offset, 0), end());
		return periodicity.range(start, end);
	}

	public Periodicity<P> periodicity() {
		return periodicity;
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
            && Objects.equals(this.startInclusive, other.startInclusive)
            && Objects.equals(this.endInclusive, other.endInclusive);
	}
	
	@Override
	public String toString() {
		return Joiner.on(":").join(periodicity.code(),start().toString(),end().toString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(periodicity.code(),startInclusive.hashCode(),endInclusive.hashCode());
	}
}
