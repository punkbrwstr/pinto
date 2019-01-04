package tech.pinto.time;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Periodicity<P extends Period<P>> {
	
	public abstract P get(long index);
	
	public abstract P from(LocalDate date);
	
	public abstract String code();

	public abstract String bloombergCode();
	
	public abstract double annualizationFactor();
	

	public P next(P arg0) {
		return offset(1,arg0);
	}

	public P previous(P arg0) {
		return offset(-1,arg0);
	}

	public long distance(P arg0, P period) {
		return period.longValue() - arg0.longValue();
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

	public LocalDate offset(long count, LocalDate start) {
		return get(from(start).longValue() + count).endDate();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PeriodicRange<P> range(Period start, Period end) {
		return new PeriodicRange(this, start, end);
	}

	public PeriodicRange<P> range(LocalDate start, LocalDate end) {
		return range(from(start),from(end));
	}
	
	public PeriodicRange<P> range(long start, long end) {
		return range(get(start),get(end));
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

	public P previous(LocalDate endDate) {
		return from(endDate).previous();
	}

	public P next(LocalDate endDate) {
		return from(endDate).next();
	}
	

}
