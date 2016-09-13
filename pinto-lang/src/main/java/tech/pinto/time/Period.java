package tech.pinto.time;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public abstract class Period implements Comparable<Period> {
	
		final protected long value;

		public abstract LocalDate endDate();

		protected abstract Period makeSame(long value);
		
		protected Period(long value) {
			this.value = value;
		}

		public long longValue() {
			return value;
		}
		
		public boolean isBefore(Period other) {
			return compareTo(other) == -1;
		}

		public boolean isAfter(Period other) {
			return compareTo(other) == 1;
		}
		
		public Period next() {
			return offset(1);
		}

		public Period previous() {
			return offset(-1);
		}
		
		public Period offset(long value) {
			return makeSame(this.value + value);
		}
		
		public long dayCount() {
			return previous().endDate().until(endDate(),ChronoUnit.DAYS);
		}
		
		public double yearFrac() {
			return dayCount() / (double) Year.from(endDate()).length();
		}
		
		@Override
		public int compareTo(Period other) {
			if(! getClass().equals(other.getClass())) {
				throw new IllegalArgumentException("Periods can only be compared to other "
						+ "Periods of the same type.");
			}
			return value == other.value ? 0 : value > other.value ? 1 : -1;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
	        if (getClass() != obj.getClass()) return false;
	        final Period other = (Period) obj;
	        return other.value == value;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(getClass(), value);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("end", endDate()).toString();
		}
		
}
