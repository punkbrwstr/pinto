package tech.pinto.function.intermediate;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.IntermediateFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Join extends IntermediateFunction {

	private final List<LocalDate> cutoverDates;

	public Join(LinkedList<Function> inputs, String... args) {
		super("join", inputs, args);
		cutoverDates = Stream.of(args).map(LocalDate::parse).collect(Collectors.toList());
		outputCount = 1;
	}

	
	@Override public Function getReference() {
		return this;
	}
	
	
	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		DoubleStream ds = DoubleStream.empty().sequential();
		
		List<P> cutoverPeriods = cutoverDates.stream().map(range.periodicity()::from).collect(Collectors.toList());
		P current = range.start();
		int i = 0;
		
		while(i < cutoverPeriods.size() && ! current.isAfter(range.end())) {
			Function currentFunction = inputStack.removeFirst();
			if(current.isBefore(cutoverPeriods.get(i))) {
				P chunkEnd = range.end().isBefore(cutoverPeriods.get(i)) ? range.end() :
						range.periodicity().previous(cutoverPeriods.get(i));
				PeriodicRange<P> chunkRange = range.periodicity().range(current, chunkEnd, range.clearCache());
				ds = DoubleStream.concat(ds, currentFunction.evaluate(chunkRange).stream());
				current = range.periodicity().next(chunkEnd);
			}
			i++;
		}

		if(inputStack.isEmpty()) {
			throw new IllegalArgumentException("Not enough inputs for " + toString());
		}
		Function currentFunction = inputStack.removeFirst();
		if(!current.isAfter(range.end())) {
			PeriodicRange<P> chunkRange = range.periodicity().range(current, range.end(), range.clearCache());
			ds = DoubleStream.concat(ds, currentFunction.evaluate(chunkRange).stream());
		}
		
		return new TimeSeries(range, name, ds);
	}


	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("join")
				.outputs("1")
				.description("Fills missing data with last good obseration.")
				.parameter("date<sub>1</sub>, date<sub>z</sub>",null , "yyyy-mm-dd")
				.build();
	}

}
