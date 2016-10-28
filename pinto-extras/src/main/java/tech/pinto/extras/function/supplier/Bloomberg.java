package tech.pinto.extras.function.supplier;

import java.util.Arrays;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.Indexer;
import tech.pinto.extras.BloombergClient;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.supplier.CachedSupplierFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Bloomberg extends CachedSupplierFunction {
	
	private final java.util.function.Function<PeriodicRange<?>, List<DoubleStream>> function;
	private final List<String> securityCodeFieldCode;
	
	public Bloomberg(String name, ComposableFunction previousFunction, Indexer indexer, BloombergClient bc, String... args) {
		super(name, previousFunction, indexer, args);
		if(args.length == 0) {
			throw new IllegalArgumentException("bbg requires at least one argument");
		}
		List<String> securityCodes = Stream.of(args[0].split(":")).map(s -> s.trim())
										.collect(Collectors.toList());
		List<String> fieldCodes = args.length == 1 ? Arrays.asList("PX_LAST") :
				Stream.of(args[1].split(":")).map(s -> s.trim()).map(String::toUpperCase)
					.map(s -> s.replaceAll(" ", "_")).collect(Collectors.toList());
		function = bc.getFunction(securityCodes, fieldCodes);
		securityCodeFieldCode = securityCodes.stream()
						.flatMap(s -> fieldCodes.stream().map(c -> s + ":" + c)).collect(Collectors.toList());
	}


	@Override
	public <P extends Period> List<DoubleStream> evaluateAll(PeriodicRange<P> range) {
		return function.apply(range);
	}

	@Override
	protected int additionalOutputCount() {
		return securityCodeFieldCode.size();
	}

	@Override
	protected List<String> allLabels() {
		return securityCodeFieldCode;
	}
	
}
