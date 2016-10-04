package tech.pinto.function.intermediate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import com.google.common.base.Joiner;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.function.UnaryFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;


public class Rolling extends IntermediateFunction {

	private final Supplier<DoubleCollector> collectorSupplier;
	private int size;
	private final Optional<Periodicity<?>> windowFrequency;
	
	public Rolling(String name, LinkedList<Function> inputs, Supplier<DoubleCollector> collectorSupplier,
					boolean countIncludesCurrent, String... args) {
		super(name, inputs, args);
		this.collectorSupplier = collectorSupplier;
		if(args.length < 1) {
			if(countIncludesCurrent) {
				throw new IllegalArgumentException("window requires at least one parameter (window size)");
			} else {
				size = 2;
			}
		} else {
			try {
				size = Math.abs(Integer.parseInt(args[0].replaceAll("\\s+", "")));
				size += countIncludesCurrent ? 0 : 1;
			} catch(NumberFormatException nfe) {
				throw new IllegalArgumentException("Non-numeric argument \"" + args[0] + "\" for window"
						+ " size in rolling function args: \"" + Joiner.on(",").join(args) + "\"");
			}
		}
		if(args.length < 2) {
			windowFrequency =  Optional.empty();
		} else {
			Periodicity<?> p =	Periodicities.get(args[1].replaceAll("\\s+", ""));
			if(p == null) {
				throw new IllegalArgumentException("invalid periodicity code for window: \"" + args[1] + "\"");
			}
			windowFrequency =  Optional.of(p);
		}
		outputCount = inputStack.size();
	}
	
	@SuppressWarnings("unchecked")
	@Override public Function getReference() {
		return new UnaryFunction(toString(),inputStack.removeFirst(), f -> range -> {
			Periodicity<Period> wf = (Periodicity<Period>) windowFrequency.orElse(range.periodicity());
			Period expandedWindowStart = wf.offset(wf.from(range.start().endDate()), -1 * (size - 1));
			Period windowEnd = wf.from(range.end().endDate());
			PeriodicRange<Period> expandedWindow = wf.range(expandedWindowStart, windowEnd, range.clearCache());
			TimeSeries input = (TimeSeries) f.evaluate(expandedWindow);
			Builder b = DoubleStream.builder();
			double[] data = input.stream().toArray();
			for(Period p : range.values()) {
				long windowStartIndex = wf.distance(expandedWindowStart, wf.from(p.endDate())) - size + 1;
				DoubleCollector dc = Arrays.stream(data, (int) windowStartIndex, (int) windowStartIndex + size)
							.collect(collectorSupplier, (v,d) -> v.add(d), (v,v1) -> v.combine(v1));
				b.accept(dc.finish());
			}
			return new TimeSeries(range,joinWithSpaces(input.getLabel(),toString()),b.build());
		});
	}

	public static Supplier<FunctionHelp> getHelp(String name, String description) {
		return () -> new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Calculates " + description + " over rolling window starting *size* number of *periodicity* prior for each input.")
				.parameter("size","1",null)
				.parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}")
				.build();
	}
	
}
