package tech.pinto.function.supplier;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.NullarySimpleFunction;

public class Moon extends NullarySimpleFunction {
	

	public Moon() {
		super("moon", range ->  new TimeSeries(range,  "moon", 
				range.dates().stream().map(d -> dateToCalendar(d)).mapToDouble(c -> new tech.pinto.tools.MoonPhase(c).getPhase())));
	}

	private static Calendar dateToCalendar(LocalDate localDate) {
		java.util.Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;

	}
	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("moon")
				.outputs("n + 1")
				.description("Calculates moon phase for this day.")
				.build();
	}

}
