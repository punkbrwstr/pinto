package pinto.data;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import pinto.time.PeriodicRange;

public class DoubleData extends Data<DoubleStream> {

	public DoubleData(PeriodicRange<?> range, String label, DoubleStream data) {
		super(range, label, data);
	}

	
	public String toString() {
		return label + ": " + data.mapToObj(Double::toString).collect(Collectors.joining(",","[","]"));
	}
}
