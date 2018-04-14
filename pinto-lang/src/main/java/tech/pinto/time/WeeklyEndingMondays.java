package tech.pinto.time;

import java.time.LocalDate;

public class WeeklyEndingMondays extends Periodicity<WeekEndingMonday> {



	@Override public String code() { return "W-MON"; }

	@Override public String bloombergCode() { return "WEEKLY"; }

	@Override
	public WeekEndingMonday get(long index) {
		return new WeekEndingMonday(index);
	}

	@Override
	public WeekEndingMonday from(LocalDate date) {
		return new WeekEndingMonday(date);
	}




}
