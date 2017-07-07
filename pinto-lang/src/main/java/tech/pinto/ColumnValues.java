package tech.pinto;

import java.util.Optional;

import java.util.stream.DoubleStream;

public class ColumnValues  {

	final private Optional<String> text;
	final private Optional<DoubleStream> series;
	
	public ColumnValues(Optional<String> text, Optional<DoubleStream> series) {
		this.text = text;
		this.series = series;
	}

	public Optional<String> getText() {
		return text;
	}

	public Optional<DoubleStream> getSeries() {
		return series;
	}
}
