package tech.pinto.function.functions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import com.google.common.base.Joiner;

import tech.pinto.Indexer;
import tech.pinto.Column;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.DoubleCollector;


public class Rolling extends ComposableFunction {

	private final Supplier<DoubleCollector> collectorSupplier;
	private final boolean countIncludesCurrent;
	
	public Rolling(String name, ComposableFunction previousFunction, Indexer indexer,
			Supplier<DoubleCollector> collectorSupplier, boolean countIncludesCurrent) {
		super(name, previousFunction, indexer);
		this.collectorSupplier = collectorSupplier;
		this.countIncludesCurrent = countIncludesCurrent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		int size;
		Optional<Periodicity<?>> windowFrequency;
		if(getArgs().length < 1) {
			if(countIncludesCurrent) {
				throw new IllegalArgumentException(name + " requires at least one argument (window size)");
			} else {
				size = 2;
			}
		} else {
			try {
				size = Math.abs(Integer.parseInt(getArgs()[0].replaceAll("\\s+", "")));
				size += countIncludesCurrent ? 0 : 1;
			} catch(NumberFormatException nfe) {
				throw new IllegalArgumentException("Non-numeric argument \"" + getArgs()[0] + "\" for window"
						+ " size in rolling function args: \"" + Joiner.on(",").join(getArgs()) + "\"");
			}
		}
		int finalSize = size;
		if(getArgs().length < 2) {
			windowFrequency =  Optional.empty();
		} else {
			Periodicity<?> p =	Periodicities.get(getArgs()[1].replaceAll("\\s+", ""));
			if(p == null) {
				throw new IllegalArgumentException("invalid periodicity code for window: \"" + getArgs()[1] + "\"");
			}
			windowFrequency =  Optional.of(p);
		}
		LinkedList<Column> outputs = new LinkedList<>();
		for (Column function : stack) {
			outputs.add(new Column(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				Periodicity<Period> wf = (Periodicity<Period>) windowFrequency.orElse(range.periodicity());
				Period expandedWindowStart = wf.offset(wf.from(range.start().endDate()), -1 * (finalSize - 1));
				Period windowEnd = wf.from(range.end().endDate());
				PeriodicRange<Period> expandedWindow = wf.range(expandedWindowStart, windowEnd, range.clearCache());
				Builder b = DoubleStream.builder();
				double[] data = inputs[0].getCells(expandedWindow).toArray();
				for(Period p : range.values()) {
					long windowStartIndex = wf.distance(expandedWindowStart, wf.from(p.endDate())) - finalSize + 1;
					DoubleCollector dc = Arrays.stream(data, (int) windowStartIndex, (int) windowStartIndex + finalSize)
								.collect(collectorSupplier, (v,d) -> v.add(d), (v,v1) -> v.combine(v1));
					b.accept(dc.finish());
				}
				return b.build();
			}, function));
		}
		return outputs;
	
	}
	
	public static FunctionHelp getHelp(String name, String description) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Calculates " + description + " over rolling window starting *size* number of *periodicity* prior for each input.")
				.parameter("size","1",null)
				.parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}")
				.build();
	}
}
