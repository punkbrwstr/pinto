package tech.pinto.tests.command;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;

import tech.pinto.Cache;
import tech.pinto.command.nonedouble.CachedDoubleCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class CallCounter extends CachedDoubleCommand {
	
	private static AtomicInteger count = new AtomicInteger();

	public CallCounter(Cache cache, String...args) {
		super("counter", cache, args);
		inputCount = 0;
		outputCount = 1;
	}

	@Override
	public <P extends Period> List<DoubleData> evaluateAllUncached(PeriodicRange<P> range) {
		double d = count.getAndIncrement();
		return Arrays.asList(new DoubleData(range, toString(), 
				DoubleStream.iterate(d, r -> d ).limit(range.size())));
	}


}
