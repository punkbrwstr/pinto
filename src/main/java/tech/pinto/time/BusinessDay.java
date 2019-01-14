package tech.pinto.time;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BusinessDay extends Period<BusinessDay> {
	
	static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);
	
	BusinessDay(long value) {
		super(value);
	}

	BusinessDay(LocalDate day) {
		this(distance(EPOCH,day));
	}

	@Override
	public LocalDate endDate() {
		return offset(EPOCH, value);
	}
	
	private static long distance(LocalDate d1, LocalDate d2) {
		int w1 = d1.getDayOfWeek().getValue() % 7 + 1; // match old convention of sunday = 1
		d1 = d1.plusDays(-w1);
		int w2 = d2.getDayOfWeek().getValue() % 7 + 1; 
		d2 = d2.plusDays(-w2);
		long days = ChronoUnit.DAYS.between(d1, d2);
		long daysWithoutWeekendDays = days - (days * 2 / 7);
		if (w1 == 1) {
			w1 = 2;
		}
		if (w2 == 1) {
			w2 = 2;
		}
		return daysWithoutWeekendDays - w1 + w2;
	}
	
	private static LocalDate offset(LocalDate rounded, long count) {
		if (count == 0) {
			return rounded;
		} else if (count > 0) {
			long daysToPrevMonday = rounded.getDayOfWeek().getValue() - 1;
			LocalDate prevMonday = rounded.minusDays(daysToPrevMonday);
			long weekendsInDistance = (count + daysToPrevMonday) / 5; // int
																		// division
			return prevMonday.plusDays(count + weekendsInDistance * 2 + daysToPrevMonday);
		} else {
			int daysToNextFriday = rounded.getDayOfWeek().getValue() - 5;
			LocalDate nextFriday = rounded.minusDays(daysToNextFriday);
			long weekendsInDistance = (count + daysToNextFriday) / 5; // int
																		// division
			return nextFriday.plusDays(count + weekendsInDistance * 2 + daysToNextFriday);
		}
	}

	@Override
	public BusinessDay makeSame(long value) {
		return new BusinessDay(value);
	}


}
