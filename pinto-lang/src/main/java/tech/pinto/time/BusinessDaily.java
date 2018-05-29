package tech.pinto.time;

import java.time.LocalDate;

public class BusinessDaily extends Periodicity<BusinessDay> {

	@Override public String code() { return "B"; }

	@Override public String bloombergCode() { return "DAILY"; }

	@Override
	public BusinessDay get(long index) {
		return new BusinessDay(index);
	}

	@Override
	public BusinessDay from(LocalDate date) {
		return new BusinessDay(date);
	}

	@Override
	public double annualizationFactor() {
		return 252;
	}

}
