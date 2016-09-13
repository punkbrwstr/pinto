package tech.pinto.time;

import java.time.LocalDate;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public abstract class Periodicity<P extends Period> extends DiscreteDomain<P> {
	
	public abstract P get(long index);
	
	public abstract P from(LocalDate date);
	
	public abstract String code();

	public abstract String bloombergCode();
	

	@Override
	public P next(P arg0) {
		return offset(arg0,1);
	}

	@Override
	public P previous(P arg0) {
		return offset(arg0,-1);
	}

	@Override
	public long distance(P arg0, P arg1) {
		return arg1.longValue() - arg0.longValue();
	}

	public long distance(LocalDate arg0, LocalDate arg1) {
		return from(arg1).longValue() - from(arg0).longValue();
	}
	
	public P roundDown(LocalDate date) {
		P p = from(date);
		return p.endDate().isAfter(date) ? previous(p) : p;
	}

	public P roundUp(LocalDate date) {
		P p = from(date);
		return p.endDate().isBefore(date) ? next(p) : p;
	}
	
	public P offset(P start, long count) {
		return get(start.longValue() + count);
	}

	@SuppressWarnings("unchecked")
	public PeriodicRange<P> range(Period start, Period end, boolean clearCache) {
		return new PeriodicRange<P>(this, (Range<P>) Range.closed(start, end), clearCache);
	}

	public PeriodicRange<P> range(LocalDate start, LocalDate end, boolean clearCache) {
		return range(from(start),from(end), clearCache);
	}

	@Override
	public String toString() {
		return code();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		return code().equals(((Periodicity) obj).code());
	}

	@Override
	public int hashCode() {
		return code().hashCode();
	}
	
	
	
	

}
