package tech.pinto.time;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class WeekEndingMonday extends Period {
	
	static final LocalDate EPOCH = LocalDate.of(1970, 1, 5);
	
	WeekEndingMonday(long value) {
		super(value);
	}
	
	WeekEndingMonday(LocalDate friday) {
		this(EPOCH.until(round(friday),ChronoUnit.WEEKS));
	}
	
	@Override
	public LocalDate endDate() {
		return EPOCH.plusWeeks(value);
	}

	@Override
	protected Period makeSame(long value) {
		return new WeekEndingMonday(value);
	}
	
	private static LocalDate round(LocalDate d) {
		long dow = d.get(ChronoField.DAY_OF_WEEK);
		long days = dow == 1 ? 0 : 8 - dow;
		return d.plusDays(days);
	}

}
