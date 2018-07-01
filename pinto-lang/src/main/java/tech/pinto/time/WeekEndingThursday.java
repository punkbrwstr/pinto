package tech.pinto.time;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class WeekEndingThursday extends Period<WeekEndingThursday> {
	
	static final LocalDate EPOCH = LocalDate.of(1970, 1, 8);
	
	WeekEndingThursday(long value) {
		super(value);
	}
	
	WeekEndingThursday(LocalDate thursday) {
		this(EPOCH.until(round(thursday),ChronoUnit.WEEKS));
	}
	
	@Override
	public LocalDate endDate() {
		return EPOCH.plusWeeks(value);
	}

	@Override
	protected WeekEndingThursday makeSame(long value) {
		return new WeekEndingThursday(value);
	}
	
	private static LocalDate round(LocalDate d) {
		long dow = d.get(ChronoField.DAY_OF_WEEK);
		long days = dow <= 4 ? 4 - dow : 11 - dow;
		return d.plusDays(days);
	}

}
