package tech.pinto.tests.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;

import tech.pinto.Cache;
import tech.pinto.TimeSeries;
import tech.pinto.function.Function;
import tech.pinto.function.supplier.CachedSupplierFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class CallCounter extends CachedSupplierFunction {
	
	private static AtomicInteger count = new AtomicInteger();

	public CallCounter(Cache cache, LinkedList<Function> inputs, String...args) {
		super("counter", cache,inputs, args);
	}

	@Override
	public <P extends Period> List<TimeSeries> evaluateAllUncached(PeriodicRange<P> range) {
		double d = count.getAndIncrement();
		return Arrays.asList(new TimeSeries(range, toString(), 
				DoubleStream.iterate(d, r -> d ).limit(range.size())));
	}

	@Override
	protected int myOutputCount() {
		return 1;
	}


}
