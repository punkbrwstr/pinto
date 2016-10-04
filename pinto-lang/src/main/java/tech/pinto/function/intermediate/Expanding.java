package tech.pinto.function.intermediate;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;


public class Expanding extends IntermediateFunction {

	private final Supplier<DoubleCollector> collectorSupplier;
	private final Optional<LocalDate> start;
	private final Optional<Periodicity<?>> windowFrequency;
	
	public Expanding(String name, LinkedList<Function> inputs, Supplier<DoubleCollector> collectorSupplier, String... args) {
		super(name, inputs, args);
		this.collectorSupplier = collectorSupplier;
		start = args.length == 0 ? Optional.empty() : Optional.of(LocalDate.parse(args[0]));
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
	
	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		@SuppressWarnings("unchecked")
		Periodicity<Period> wf = (Periodicity<Period>) windowFrequency.orElse(range.periodicity());
		LocalDate startDate = start.orElse(range.start().endDate());
		Period windowStart = wf.from(startDate);
		Period windowEnd = wf.from(range.end().endDate());
		windowEnd = windowEnd.isBefore(windowStart) ? windowStart : windowEnd;
		PeriodicRange<Period> window = wf.range(windowStart, windowEnd, range.clearCache());
		
		DoubleStream.Builder b = DoubleStream.builder();
		TimeSeries input = null;
		DoubleCollector dc = collectorSupplier.get();
		input = (TimeSeries) inputStack.removeFirst().evaluate(window);
		double[] output = input.stream().map(d -> {
				dc.add(d);
				return dc.finish();
		}).toArray();
		for(Period p : range.values()) {
			int index = (int) window.indexOf(p.endDate());
			if(index >= 0) {
				b.accept(output[index]);
			} else  {
				b.accept(Double.NaN);
			}	
		}
		return new TimeSeries(range,joinWithSpaces(input.getLabel(),toString()),b.build());
	}
	
	public static Supplier<FunctionHelp> getHelp(String name, String description) {
		return () -> new FunctionHelp.Builder(name)
				.inputs("double<sub>1</sub>...double<sub>n</sub>")
				.outputs("double<sub>1</sub>...double<sub>n</sub>")
				.description("Calculates " + description + " over an expanding window starting *start_date* over *periodicity* for *n* inputs.")
				.parameter("start_date","1",null)
				.parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}")
				.parameter("n","all",null)
				.build();
	}

	@Override
	public Function getReference() {
		return this;
	}
	
}
