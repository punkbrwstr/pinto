package tech.pinto.tests.command;

import java.util.ArrayDeque;

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
	public <P extends Period> ArrayDeque<DoubleData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<DoubleData> output = new ArrayDeque<>();
		double d = count.getAndIncrement();
		output.addFirst(new DoubleData(range, toString(), 
				DoubleStream.iterate(d, r -> d ).limit(range.size())));
		return output;
	}


}
