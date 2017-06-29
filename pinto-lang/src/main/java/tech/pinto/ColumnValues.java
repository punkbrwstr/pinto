package tech.pinto;

import java.util.stream.DoubleStream;

import tech.pinto.time.PeriodicRange;

public class ColumnValues  {

	final private PeriodicRange<?> range;
	final private String text;
	final private DoubleStream series;
	
	public ColumnValues(PeriodicRange<?> range, String text, DoubleStream series) {
		this.range = range;
		this.text = text;
		this.series = series;
	}

	public PeriodicRange<?> getRange() {
		return range;
	}

	public String getText() {
		return text;
	}

	public DoubleStream getSeries() {
		return series;
	}
}
