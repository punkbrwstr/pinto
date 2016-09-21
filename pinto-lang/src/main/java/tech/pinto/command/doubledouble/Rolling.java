package tech.pinto.command.doubledouble;

import java.util.Arrays;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import com.google.common.base.Joiner;

import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;


public class Rolling extends ParameterizedCommand {

	private final Supplier<DoubleCollector> collectorSupplier;
	private int size;
	private final Optional<Periodicity<?>> windowFrequency;
	
	public Rolling(String name, Supplier<DoubleCollector> collectorSupplier,
					boolean countIncludesCurrent, String... args) {
		super(name, DoubleData.class, DoubleData.class, args);
		this.collectorSupplier = collectorSupplier;
		if(args.length < 1) {
			throw new IllegalArgumentException("window requires at least one parameter (window size)");
		}
		try {
			size = Math.abs(Integer.parseInt(args[0].replaceAll("\\s+", "")));
			size += countIncludesCurrent ? 0 : 1;
		} catch(NumberFormatException nfe) {
			throw new IllegalArgumentException("Non-numeric argument \"" + args[0] + "\" for window"
					+ " size in rolling function args: \"" + Joiner.on(",").join(args) + "\"");
		}
		if(args.length == 1) {
			windowFrequency =  Optional.empty();
		} else {
			Periodicity<?> p =	Periodicities.get(args[1].replaceAll("\\s+", ""));
			if(p == null) {
				throw new IllegalArgumentException("invalid periodicity code for window: \"" + args[1] + "\"");
			}
			windowFrequency =  Optional.of(p);
		}
		inputCount = args.length <= 2 || Integer.parseInt(args[2]) == -1 ? Integer.MAX_VALUE : Integer.parseInt(args[2]);
	}
	
	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size();
	}


	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		@SuppressWarnings("unchecked")
		Periodicity<Period> wf = (Periodicity<Period>) windowFrequency.orElse(range.periodicity());
		Period expandedWindowStart = wf.offset(wf.from(range.start().endDate()), -1 * (size - 1));
		Period windowEnd = wf.from(range.end().endDate());
		PeriodicRange<Period> expandedWindow = wf.range(expandedWindowStart, windowEnd, range.clearCache());
		DoubleData input = (DoubleData) inputStack.removeFirst().evaluate(expandedWindow);
		Builder b = DoubleStream.builder();
		double[] data = input.getData().toArray();
		for(Period p : range.values()) {
			long windowStartIndex = wf.distance(expandedWindowStart, wf.from(p.endDate())) - size + 1;
			DoubleCollector dc = Arrays.stream(data, (int) windowStartIndex, (int) windowStartIndex + size)
						.collect(collectorSupplier, (v,d) -> v.add(d), (v,v1) -> v.combine(v1));
			b.accept(dc.finish());
		}
		return new DoubleData(range,joinWithSpaces(input.getLabel(),toString()),b.build());
	}
	
}
