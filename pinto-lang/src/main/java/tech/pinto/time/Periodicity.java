package tech.pinto.time;

import java.time.LocalDate;
import java.util.Objects;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public abstract class Periodicity<P extends Period> extends DiscreteDomain<P> {
	
	public abstract P get(long index);
	
	public abstract P from(LocalDate date);
	
	public abstract String code();

	public abstract String bloombergCode();
	

	@Override
	public P next(P arg0) {
		return offset(1,arg0);
	}

	@Override
	public P previous(P arg0) {
		return offset(-1,arg0);
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
	
	public P offset(long count, P start) {
		return get(start.longValue() + count);
	}

	@SuppressWarnings("unchecked")
	public PeriodicRange<P> range(Period start, Period end, boolean clearCache) {
		return new PeriodicRange<P>(this, (Range<P>) Range.closed(start, end), clearCache);
	}

	public PeriodicRange<P> range(LocalDate start, LocalDate end, boolean clearCache) {
		return range(from(start),from(end), clearCache);
	}
	
	public PeriodicRange<P> range(long start, long end, boolean clearCache) {
		return range(get(start),get(end), clearCache);
	}

	@Override
	public String toString() {
		return code();
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Periodicity<?> other = (Periodicity<?>) obj;
        return Objects.equals(this.code(), other.code());
	}

	@Override
	public int hashCode() {
		return code().hashCode();
	}
	

}
