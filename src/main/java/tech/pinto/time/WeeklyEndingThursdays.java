package tech.pinto.time;

import java.time.LocalDate;

public class WeeklyEndingThursdays extends Periodicity<WeekEndingThursday> {



	@Override public String code() { return "W-THU"; }

	@Override public String bloombergCode() { return "WEEKLY"; }

	@Override
	public WeekEndingThursday get(long index) {
		return new WeekEndingThursday(index);
	}

	@Override
	public WeekEndingThursday from(LocalDate date) {
		return new WeekEndingThursday(date);
	}

	@Override
	public double annualizationFactor() {
		return 52;
	}




}
