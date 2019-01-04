package tech.pinto.time;

import java.time.LocalDate;

public class BusinessYearly extends Periodicity<BusinessYear> {

	@Override
	public BusinessYear get(long index) {
		return new BusinessYear(index);
	}

	@Override
	public BusinessYear from(LocalDate date) {
		return new BusinessYear(date);
	}

	@Override public String code() { return "BA-DEC"; }

	@Override public String bloombergCode() { return "YEARLY"; }

	@Override
	public double annualizationFactor() {
		return 1;
	}

	
}
