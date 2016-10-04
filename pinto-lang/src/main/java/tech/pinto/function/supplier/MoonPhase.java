package tech.pinto.function.supplier;

import java.time.LocalDate;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class MoonPhase extends Function {
	
	public MoonPhase() {
		super("moon", new LinkedList<>());
		outputCount = 1;
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		DoubleStream ds = range.dates().stream().map(d -> dateToCalendar(d))
				.mapToDouble(c -> new tech.pinto.tools.MoonPhase(c).getPhase());
		return new TimeSeries(range, toString(), ds);
	}
	
	private Calendar dateToCalendar(LocalDate localDate) {
		java.util.Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;

	}
	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("moon")
				.inputs("none")
				.outputs("double<sub>1</sub>...double<sub>z</sub>")
				.description("Calculates moon phase for this day.")
				.build();
	}

	@Override
	public Function getReference() {
		return this;
	}

}
