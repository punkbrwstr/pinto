package tech.pinto.extras.function.supplier;

import java.util.ArrayList;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.Indexer;
import tech.pinto.extras.BloombergClient;
import tech.pinto.function.CachedFunction;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ParameterType;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Bloomberg extends CachedFunction {

	private List<String> securityCodes = new ArrayList<>();
	private List<String> fieldCodes = new ArrayList<>();
	private List<String> securityCodeFieldCode = new ArrayList<>();
	private final BloombergClient bc;

	public Bloomberg(String name, ComposableFunction previousFunction, Indexer indexer, BloombergClient bc) {
		super(name, previousFunction, indexer,ParameterType.arguments_required);
		this.bc = bc;
	}

	@Override
	protected <P extends Period> List<DoubleStream> getUncachedSeries(PeriodicRange<P> range) {
		parseArgs();
		return bc.getFunction(securityCodes, fieldCodes).apply(range);
	}

	@Override
	protected List<String> getUncachedText() {
		return securityCodeFieldCode;
	}

	@Override
	protected int columns() {
		if (securityCodes.size() == 0) {
			parseArgs();
		}
		return securityCodeFieldCode.size();
	}

	private void parseArgs() {
		if (getArgs().length == 0) {
			throw new IllegalArgumentException("bbg requires at least one argument");
		} else if (securityCodes.size() == 0) {
			Stream.of(getArgs()[0].split(":")).map(s -> s.trim()).forEach(securityCodes::add);
			if(getArgs().length == 1) {
				fieldCodes.add("PX_LAST");
			} else {
				Stream.of(getArgs()[1].split(":")).map(s -> s.trim()).map(String::toUpperCase)
							.map(s -> s.replaceAll(" ", "_")).forEach(fieldCodes::add);
			}
			securityCodes.stream().flatMap(s -> fieldCodes.stream().map(c -> s + ":" + c))
					.forEach(securityCodeFieldCode::add);
		}
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name).outputs("n + t * f")
				.description("Retrieves online price history for each ticker and field combination.")
				.parameter("ticker<sub>1</sub>:ticker<sub>t</sub>")
				.parameter("field<sub>1</sub>:field<sub>f</sub>", "PX_LAST", "").build();
	}


}
