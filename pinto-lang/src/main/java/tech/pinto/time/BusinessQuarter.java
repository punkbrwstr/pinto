package tech.pinto.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class BusinessQuarter extends Period {
	
	static final private YearMonth EPOCH = YearMonth.of(1969, 12);

	BusinessQuarter(long value) {
		super(value);
	}
	
	BusinessQuarter(LocalDate day) {
		this(EPOCH.until(round(day),ChronoUnit.MONTHS) / 3);
	}


	@Override
	public LocalDate endDate() {
		LocalDate d = EPOCH.plusMonths(value * 3).atEndOfMonth();
		d = d.getDayOfWeek().equals(DayOfWeek.SATURDAY) ? d.minusDays(1l) : d;
		d = d.getDayOfWeek().equals(DayOfWeek.SUNDAY) ? d.minusDays(2l) : d;
		return d;
	}

	@Override
	protected Period makeSame(long value) {
		return new BusinessQuarter(value);
	}

	private static YearMonth round(LocalDate d) {
		YearMonth ym = YearMonth.from(d);
		long remainder = d.get(ChronoField.MONTH_OF_YEAR) % 3;
		long plusMonths = 	remainder == 0 ? 0 : 2 / remainder;
		return ym.plusMonths(plusMonths);
	}

}
