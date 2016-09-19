package tech.pinto.extras.command.nonedouble;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Cache;
import tech.pinto.command.nonedouble.CachedDoubleCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.extras.BloombergClient;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Bloomberg extends CachedDoubleCommand {
	
	private final Function<PeriodicRange<?>, List<DoubleData>> function;
	private int evalCount = 0;
	private Map<PeriodicRange<?>,List<DoubleData>> data = new HashMap<>();

	public Bloomberg(BloombergClient bc, Cache cache, String... arguments) {
		super("bbg", cache, arguments);
		if(arguments.length == 0) {
			throw new IllegalArgumentException("bbg requires at least one argument");
		}
		List<String> securityCodes = Stream.of(arguments[0].split(":")).map(s -> s.trim())
										.collect(Collectors.toList());
		List<String> fieldCodes = arguments.length == 1 ? Arrays.asList("PX_LAST") :
				Stream.of(arguments[1].split(":")).map(s -> s.trim()).map(String::toUpperCase)
					.map(s -> s.replaceAll(" ", "_")).collect(Collectors.toList());
		function = bc.getFunction(securityCodes, fieldCodes);
		inputCount = 0;
		outputCount = securityCodes.size() * fieldCodes.size();
	}

	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		List<DoubleData> d = data.containsKey(range) ? data.get(range) : function.apply(range);
		return d.get(evalCount++);
	}

}
