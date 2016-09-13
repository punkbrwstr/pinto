package tech.pinto.time;

import java.time.LocalDate;

public class BusinessQuarterly extends Periodicity<BusinessQuarter> {

	@Override
	public BusinessQuarter get(long index) {
		return new BusinessQuarter(index);
	}

	@Override
	public BusinessQuarter from(LocalDate date) {
		return new BusinessQuarter(date);
	}

	@Override public String code() { return "BQ-DEC"; }

	@Override public String bloombergCode() { return "QUARTERLY"; }

	
}
