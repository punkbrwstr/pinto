package tech.pinto.extras.function.supplier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Cache;
import tech.pinto.TimeSeries;
import tech.pinto.extras.BloombergClient;
import tech.pinto.function.Function;
import tech.pinto.function.supplier.CachedDoubleCommand;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Bloomberg extends CachedDoubleCommand {
	
	private final java.util.function.Function<PeriodicRange<?>, List<TimeSeries>> function;
	private final int myOutputCount;

	public Bloomberg(BloombergClient bc, Cache cache, LinkedList<Function> inputs, String... arguments) {
		super("bbg", cache, inputs, arguments);
		if(arguments.length == 0) {
			throw new IllegalArgumentException("bbg requires at least one argument");
		}
		List<String> securityCodes = Stream.of(arguments[0].split(":")).map(s -> s.trim())
										.collect(Collectors.toList());
		List<String> fieldCodes = arguments.length == 1 ? Arrays.asList("PX_LAST") :
				Stream.of(arguments[1].split(":")).map(s -> s.trim()).map(String::toUpperCase)
					.map(s -> s.replaceAll(" ", "_")).collect(Collectors.toList());
		function = bc.getFunction(securityCodes, fieldCodes);
		myOutputCount = securityCodes.size() * fieldCodes.size();
	}
	

	@Override
	public <P extends Period> List<TimeSeries> evaluateAllUncached(PeriodicRange<P> range) {
		return function.apply(range);
	}


	@Override
	protected int myOutputCount() {
		return myOutputCount;
	}

}
