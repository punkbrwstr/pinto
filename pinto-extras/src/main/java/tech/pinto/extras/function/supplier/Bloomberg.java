package tech.pinto.extras.function.supplier;

import java.util.ArrayList;

import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.extras.BloombergClient;
import tech.pinto.function.CachedFunction;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Bloomberg extends CachedFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("tickers", true, "Bloomberg ticker codes")
			.add("fields", "PX_LAST", "Bloomberg field codes");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.description("Retrieves online price history for each ticker and field combination.")
			.parameters(PARAMETERS_BUILDER.build());

	private List<String> securityCodes = new ArrayList<>();
	private List<String> fieldCodes = new ArrayList<>();
	private List<String> securityCodeFieldCode = new ArrayList<>();
	private final BloombergClient bc;

	public Bloomberg(String name, ComposableFunction previousFunction, Indexer indexer, BloombergClient bc) {
		super(name, previousFunction, indexer);
		this.bc = bc;
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
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
		if (securityCodes.size() == 0) {
			Stream.of(parameters.get().getArgument("tickers")).map(s -> s.trim()).forEach(securityCodes::add);
			Stream.of(parameters.get().getArgument("fields")).map(s -> s.trim()).map(String::toUpperCase)
							.map(s -> s.replaceAll(" ", "_")).forEach(fieldCodes::add);
			securityCodes.stream().flatMap(s -> fieldCodes.stream().map(c -> s + ":" + c))
					.forEach(securityCodeFieldCode::add);
		}
	}

}
