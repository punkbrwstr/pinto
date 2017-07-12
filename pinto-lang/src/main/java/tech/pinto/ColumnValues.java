package tech.pinto;

import java.util.Optional;

import java.util.stream.DoubleStream;

public class ColumnValues  {

	final private Optional<String> header;
	final private Optional<DoubleStream> series;
	
	public ColumnValues(Optional<String> header, Optional<DoubleStream> series) {
		this.header = header;
		this.series = series;
	}

	public Optional<String> getHeader() {
		return header;
	}

	public Optional<DoubleStream> getSeries() {
		return series;
	}
}
