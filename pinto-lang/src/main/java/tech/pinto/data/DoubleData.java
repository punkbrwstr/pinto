package tech.pinto.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import tech.pinto.time.PeriodicRange;

public class DoubleData extends Data<DoubleStream> {

	public DoubleData(PeriodicRange<?> range, String label, DoubleStream data) {
		super(range, label, data);
	}

	
	public String toString() {
		return label + ": " + data.mapToObj(Double::toString).collect(Collectors.joining(",","[","]"));
	}
	
	public static List<DoubleData> dup(List<DoubleData> a) {
		return dup(a,0);
	}

	public static List<DoubleData> dup(List<DoubleData> a, int length) {
    	List<DoubleData> b = new ArrayList<>();
    	List<DoubleData> temp = new ArrayList<>(a);
    	a.clear();
    	for(DoubleData d : temp) {
    		DoubleStream.Builder aBuilder = DoubleStream.builder();
    		DoubleStream.Builder bBuilder = DoubleStream.builder();
    		d.getData().peek(aBuilder::accept).forEachOrdered(bBuilder::accept);
    		a.add(new DoubleData(d.getRange(),d.getLabel(),aBuilder.build()));
    		b.add(new DoubleData(d.getRange(),d.getLabel(),bBuilder.build().limit(length)));
    	}
    	return b;
    }

	public static List<DoubleData> dup(DoubleData a) {
    	List<DoubleData> b = new ArrayList<>();
    	DoubleStream.Builder aBuilder = DoubleStream.builder();
    	DoubleStream.Builder bBuilder = DoubleStream.builder();
    	a.getData().peek(aBuilder::accept).forEachOrdered(bBuilder::accept);
    	b.add(new DoubleData(a.getRange(),a.getLabel(),aBuilder.build()));
    	b.add(new DoubleData(a.getRange(),a.getLabel(),bBuilder.build()));
    	return b;
    }
}
