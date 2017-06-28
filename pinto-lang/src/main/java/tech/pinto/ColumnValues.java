package tech.pinto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import tech.pinto.time.PeriodicRange;

public class ColumnValues  {

	private PeriodicRange<?> range;
	private String text;
	private DoubleStream series;
	
	public ColumnValues(PeriodicRange<?> range, String text, DoubleStream series) {
		this.range = range;
		this.text = text;
		this.series = series;
	}

	public PeriodicRange<?> getRange() {
		return range;
	}


	public void setRange(PeriodicRange<?> range) {
		this.range = range;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public DoubleStream getSeries() {
		return series;
	}


	public void setStream(DoubleStream stream) {
		this.series = stream;
	}
	
	public ColumnValues clone() {
		List<ColumnValues> copied = ColumnValues.dup(this);
		setStream(copied.get(0).getSeries());
		return copied.get(1);
	}


	public static List<ColumnValues> dup(List<ColumnValues> a) {
		return dup(a,0);
	}
	
	@SuppressWarnings("unused")
	private void peek() {
		DoubleStream.Builder b = DoubleStream.builder();
		series.peek(System.out::println).forEachOrdered(b::accept); // trick to duplicate streams
		series = b.build();
	}

	public static List<ColumnValues> dup(List<ColumnValues> a, int length) {
    	List<ColumnValues> b = new ArrayList<>();
    	List<ColumnValues> temp = new ArrayList<>(a);
    	a.clear();
    	for(ColumnValues d : temp) {
    		DoubleStream.Builder aBuilder = DoubleStream.builder();
    		DoubleStream.Builder bBuilder = DoubleStream.builder();
    		d.getSeries().peek(aBuilder::accept).forEachOrdered(bBuilder::accept);
    		a.add(new ColumnValues(d.getRange(),d.getText(),aBuilder.build()));
    		b.add(new ColumnValues(d.getRange(),d.getText(),bBuilder.build().limit(length)));
    	}
    	return b;
    }

	public static List<ColumnValues> dup(ColumnValues a) {
    	List<ColumnValues> b = new ArrayList<>();
    	DoubleStream.Builder aBuilder = DoubleStream.builder();
    	DoubleStream.Builder bBuilder = DoubleStream.builder();
    	a.getSeries().peek(aBuilder::accept).forEachOrdered(bBuilder::accept);
    	b.add(new ColumnValues(a.getRange(),a.getText(),aBuilder.build()));
    	b.add(new ColumnValues(a.getRange(),a.getText(),bBuilder.build()));
    	return b;
    }
}
