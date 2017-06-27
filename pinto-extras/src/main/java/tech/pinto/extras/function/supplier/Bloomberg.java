package tech.pinto.extras.function.supplier;

import java.util.Arrays;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.Indexer;
import tech.pinto.extras.BloombergClient;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.supplier.CachedSupplierFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Bloomberg extends CachedSupplierFunction {
	
	private List<String> securityCodes;
	private List<String> fieldCodes;
	private List<String> securityCodeFieldCode;
	private final BloombergClient bc;
	
	public Bloomberg(String name, ComposableFunction previousFunction, Indexer indexer, BloombergClient bc, String... args) {
		super(name, previousFunction, indexer, args);
		this.bc = bc;
	}


	@Override
	public <P extends Period> List<DoubleStream> evaluateAll(PeriodicRange<P> range) {
		parseArgs();
		return bc.getFunction(securityCodes, fieldCodes).apply(range);
	}
	
	private void parseArgs() {
		if(args.length == 0) {
			throw new IllegalArgumentException("bbg requires at least one argument");
		}
		securityCodes = Stream.of(args[0].split(":")).map(s -> s.trim()).collect(Collectors.toList());
		fieldCodes = args.length == 1 ? Arrays.asList("PX_LAST") :
				Stream.of(args[1].split(":")).map(s -> s.trim()).map(String::toUpperCase)
					.map(s -> s.replaceAll(" ", "_")).collect(Collectors.toList());
		securityCodeFieldCode = securityCodes.stream()
						.flatMap(s -> fieldCodes.stream().map(c -> s + ":" + c)).collect(Collectors.toList());
	}

	@Override
	protected int additionalOutputCount() {
		if(securityCodes == null) {
			parseArgs();
		}
		return securityCodeFieldCode.size();
	}

	@Override
	protected List<String> allLabels() {
		return securityCodeFieldCode;
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n + t * f")
				.description("Retrieves online price history for each ticker and field combination.")
				.parameter("ticker<sub>1</sub>:ticker<sub>t</sub>")
				.parameter("field<sub>1</sub>:field<sub>f</sub>","PX_LAST","")
				.build();
	}
	
}
