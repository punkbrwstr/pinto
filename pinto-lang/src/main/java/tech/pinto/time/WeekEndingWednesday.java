package tech.pinto.time;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class WeekEndingWednesday extends Period<WeekEndingWednesday> {
	
	static final LocalDate EPOCH = LocalDate.of(1970, 1, 7);
	
	WeekEndingWednesday(long value) {
		super(value);
	}
	
	WeekEndingWednesday(LocalDate wednesday) {
		this(EPOCH.until(round(wednesday),ChronoUnit.WEEKS));
	}
	
	@Override
	public LocalDate endDate() {
		return EPOCH.plusWeeks(value);
	}

	@Override
	protected WeekEndingWednesday makeSame(long value) {
		return new WeekEndingWednesday(value);
	}
	
	private static LocalDate round(LocalDate d) {
		long dow = d.get(ChronoField.DAY_OF_WEEK);
		long days = dow <= 3 ? 3 - dow : 10 - dow;
		return d.plusDays(days);
	}

}
