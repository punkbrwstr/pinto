package tech.pinto.time;

import java.time.LocalDate;

public class BusinessMonthly extends Periodicity<BusinessMonth> {

	@Override
	public BusinessMonth get(long index) {
		return new BusinessMonth(index);
	}

	@Override
	public BusinessMonth from(LocalDate date) {
		return new BusinessMonth(date);
	}

	@Override public String code() { return "BM"; }

	@Override public String bloombergCode() { return "MONTHLY"; }

	
}
