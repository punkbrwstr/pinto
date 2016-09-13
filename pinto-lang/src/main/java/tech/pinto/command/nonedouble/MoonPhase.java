package tech.pinto.command.nonedouble;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.DoubleStream;

import tech.pinto.command.Command;
import tech.pinto.data.DoubleData;
import tech.pinto.data.NoneData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class MoonPhase extends Command<Object,NoneData,DoubleStream,DoubleData> {
	
	public MoonPhase() {
		super("moon", NoneData.class, DoubleData.class);
		inputCount = 0;
		outputCount = 1;
	}

	@Override
	public <P extends Period> ArrayDeque<DoubleData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<DoubleData> output = new ArrayDeque<>();
	
		DoubleStream ds = range.dates().stream().map(d -> dateToCalendar(d))
				.mapToDouble(c -> new tech.pinto.tools.MoonPhase(c).getPhase());
		output.addFirst(new DoubleData(range, toString(), ds));
		return output;
	}
	
	private Calendar dateToCalendar(LocalDate localDate) {
		java.util.Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;

	}

}
