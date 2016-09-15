package tech.pinto.command.doubledouble;

import java.util.ArrayDeque;


import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.DoubleStream;


import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;


public class Fill extends ParameterizedCommand<DoubleStream,DoubleData,DoubleStream,DoubleData> {

	public Fill(String... args) {
		super("fill", DoubleData.class, DoubleData.class, args);
		inputCount = args.length == 0 ? 1 : Math.abs(Integer.parseInt(args[0].replaceAll("\\s+", "")));
		inputCount = inputCount == -1 ? Integer.MAX_VALUE : inputCount;
		outputCount = inputCount;
	}


	@Override
	protected <P extends Period> ArrayDeque<DoubleData> evaluate(PeriodicRange<P> range) {
		ArrayDeque<DoubleData> outputs = new ArrayDeque<>();
		for(DoubleData input : getInputData(range)) {
			final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
			outputs.addFirst( new DoubleData(range, joinWithSpaces(input.getLabel(), toString()),
					input.getData().map(d -> {
						if(!Double.isNaN(d)) {
							lastGoodValue.set(d);
							return d;
						} else {
							return lastGoodValue.get();
						}
					})));
		}
		return outputs;
	}
	
}
