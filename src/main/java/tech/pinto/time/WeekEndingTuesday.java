package tech.pinto.time;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class WeekEndingTuesday extends Period<WeekEndingTuesday> {
	
	static final LocalDate EPOCH = LocalDate.of(1970, 1, 6);
	
	WeekEndingTuesday(long value) {
		super(value);
	}
	
	WeekEndingTuesday(LocalDate tuesday) {
		this(EPOCH.until(round(tuesday),ChronoUnit.WEEKS));
	}
	
	@Override
	public LocalDate endDate() {
		return EPOCH.plusWeeks(value);
	}

	@Override
	protected WeekEndingTuesday makeSame(long value) {
		return new WeekEndingTuesday(value);
	}
	
	private static LocalDate round(LocalDate d) {
		long dow = d.get(ChronoField.DAY_OF_WEEK);
		long days = dow <= 2 ? 2 - dow : 9 - dow;
		return d.plusDays(days);
	}

}
