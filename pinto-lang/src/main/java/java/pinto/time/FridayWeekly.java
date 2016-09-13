package pinto.time;

import java.time.LocalDate;

public class FridayWeekly extends Periodicity<FridayWeek> {



	@Override public String code() { return "W-FRI"; }

	@Override public String bloombergCode() { return "WEEKLY"; }

	@Override
	public FridayWeek get(long index) {
		return new FridayWeek(index);
	}

	@Override
	public FridayWeek from(LocalDate date) {
		return new FridayWeek(date);
	}




}
