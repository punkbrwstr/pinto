package tech.pinto.time;

import java.time.LocalDate;

public class WeeklyEndingTuesdays extends Periodicity<WeekEndingTuesday> {



	@Override public String code() { return "W-TUE"; }

	@Override public String bloombergCode() { return "WEEKLY"; }

	@Override
	public WeekEndingTuesday get(long index) {
		return new WeekEndingTuesday(index);
	}

	@Override
	public WeekEndingTuesday from(LocalDate date) {
		return new WeekEndingTuesday(date);
	}




}
