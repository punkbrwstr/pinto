package tech.pinto.time;

import java.time.LocalDate;

public class WeeklyEndingWednesdays extends Periodicity<WeekEndingWednesday> {



	@Override public String code() { return "W-WED"; }

	@Override public String bloombergCode() { return "WEEKLY"; }

	@Override
	public WeekEndingWednesday get(long index) {
		return new WeekEndingWednesday(index);
	}

	@Override
	public WeekEndingWednesday from(LocalDate date) {
		return new WeekEndingWednesday(date);
	}




}
