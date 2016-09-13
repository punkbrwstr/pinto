package tech.pinto.time;

import java.time.DayOfWeek;

import java.time.LocalDate;
import java.time.Month;

public class BusinessYear extends Period {
	
	BusinessYear(long value) {
		super(value);
	}
	
	BusinessYear(LocalDate day) {
		this(day.getYear());
	}


	@Override
	public LocalDate endDate() {
		LocalDate d = LocalDate.of((int) value, Month.DECEMBER, 31);
		d = d.getDayOfWeek().equals(DayOfWeek.SATURDAY) ? d.minusDays(1l) : d;
		d = d.getDayOfWeek().equals(DayOfWeek.SUNDAY) ? d.minusDays(2l) : d;
		return d;
	}

	@Override
	protected Period makeSame(long value) {
		return new BusinessYear(value);
	}

}
