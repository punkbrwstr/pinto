package tech.pinto.command.doubledouble;


import java.util.concurrent.atomic.AtomicReference;


import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;


public class Fill extends ParameterizedCommand {

	public Fill(String... args) {
		super("fill", DoubleData.class, DoubleData.class, args);
		inputCount = args.length == 0 ? 1 : Math.abs(Integer.parseInt(args[0].replaceAll("\\s+", "")));
		inputCount = inputCount == -1 ? Integer.MAX_VALUE : inputCount;
		outputCount = inputCount;
	}


	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		DoubleData input = evaluate(range);
		final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
		return new DoubleData(range, joinWithSpaces(input.getLabel(), toString()),
					input.getData().map(d -> {
						if(!Double.isNaN(d)) {
							lastGoodValue.set(d);
						}
						return lastGoodValue.get();
					}));
	}
	
}
