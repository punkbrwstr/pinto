package tech.pinto.time;

import java.time.LocalDate;

public class WeeklyEndingFridays extends Periodicity<WeekEndingFriday> {



	@Override public String code() { return "W-FRI"; }

	@Override public String bloombergCode() { return "WEEKLY"; }

	@Override
	public WeekEndingFriday get(long index) {
		return new WeekEndingFriday(index);
	}

	@Override
	public WeekEndingFriday from(LocalDate date) {
		return new WeekEndingFriday(date);
	}

	@Override
	public double annualizationFactor() {
		return 52;
	}




}
