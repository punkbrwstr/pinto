package tech.pinto.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public class BusinessMonth extends Period {
	
	static final private YearMonth EPOCH = YearMonth.of(1970, 1);

	BusinessMonth(long value) {
		super(value);
	}
	
	BusinessMonth(LocalDate day) {
		this(EPOCH.until(day,ChronoUnit.MONTHS));
	}


	@Override
	public LocalDate endDate() {
		LocalDate d = EPOCH.plusMonths(value).atEndOfMonth();
		d = d.getDayOfWeek().equals(DayOfWeek.SATURDAY) ? d.minusDays(1l) : d;
		d = d.getDayOfWeek().equals(DayOfWeek.SUNDAY) ? d.minusDays(2l) : d;
		return d;
	}

	@Override
	protected Period makeSame(long value) {
		return new BusinessMonth(value);
	}


}
