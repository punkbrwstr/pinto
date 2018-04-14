package tech.pinto.time;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class WeekEndingFriday extends Period {
	
	static final LocalDate EPOCH = LocalDate.of(1970, 1, 2);
	
	WeekEndingFriday(long value) {
		super(value);
	}
	
	WeekEndingFriday(LocalDate friday) {
		this(EPOCH.until(round(friday),ChronoUnit.WEEKS));
	}
	
	@Override
	public LocalDate endDate() {
		return EPOCH.plusWeeks(value);
	}

	@Override
	protected Period makeSame(long value) {
		return new WeekEndingFriday(value);
	}
	
	private static LocalDate round(LocalDate d) {
		long dow = d.get(ChronoField.DAY_OF_WEEK);
		long days = dow > 5 ? 7 - dow + 5 : 5 - dow;
		return d.plusDays(days);
	}

}
