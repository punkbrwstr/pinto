package tech.pinto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import tech.pinto.time.PeriodicRange;

public class TimeSeries  {

	private PeriodicRange<?> range;
	private String label;
	private DoubleStream stream;
	
	public TimeSeries(PeriodicRange<?> range, String label, DoubleStream stream) {
		this.range = range;
		this.label = label;
		this.stream = stream;
	}

	public PeriodicRange<?> getRange() {
		return range;
	}


	public void setRange(PeriodicRange<?> range) {
		this.range = range;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public DoubleStream stream() {
		return stream;
	}


	public void setStream(DoubleStream stream) {
		this.stream = stream;
	}
	
	public TimeSeries clone() {
		List<TimeSeries> copied = TimeSeries.dup(this);
		setStream(copied.get(0).stream());
		return copied.get(1);
	}


	public static List<TimeSeries> dup(List<TimeSeries> a) {
		return dup(a,0);
	}
	
	@SuppressWarnings("unused")
	private void peek() {
		DoubleStream.Builder b = DoubleStream.builder();
		stream.peek(System.out::println).forEachOrdered(b::accept); // trick to duplicate streams
		stream = b.build();
	}

	public static List<TimeSeries> dup(List<TimeSeries> a, int length) {
    	List<TimeSeries> b = new ArrayList<>();
    	List<TimeSeries> temp = new ArrayList<>(a);
    	a.clear();
    	for(TimeSeries d : temp) {
    		DoubleStream.Builder aBuilder = DoubleStream.builder();
    		DoubleStream.Builder bBuilder = DoubleStream.builder();
    		d.stream().peek(aBuilder::accept).forEachOrdered(bBuilder::accept);
    		a.add(new TimeSeries(d.getRange(),d.getLabel(),aBuilder.build()));
    		b.add(new TimeSeries(d.getRange(),d.getLabel(),bBuilder.build().limit(length)));
    	}
    	return b;
    }

	public static List<TimeSeries> dup(TimeSeries a) {
    	List<TimeSeries> b = new ArrayList<>();
    	DoubleStream.Builder aBuilder = DoubleStream.builder();
    	DoubleStream.Builder bBuilder = DoubleStream.builder();
    	a.stream().peek(aBuilder::accept).forEachOrdered(bBuilder::accept);
    	b.add(new TimeSeries(a.getRange(),a.getLabel(),aBuilder.build()));
    	b.add(new TimeSeries(a.getRange(),a.getLabel(),bBuilder.build()));
    	return b;
    }
}
