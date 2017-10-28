package tech.pinto.function.functions.terminal;

import java.time.LocalDate;


import java.util.Optional;
import java.util.stream.Collectors;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Parameters;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.Periodicities;

public class Evaluate extends TerminalFunction {
	protected static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("start", LocalDate.now().toString(), "Start date of range to evaluate (format: yyyy-mm-dd)")
			.add("end", LocalDate.now().toString(), "End date of range to evaluate (format: yyyy-mm-dd)")
			.add("freq", "B", "Periodicity of range to evaluate " +
						Periodicities.allCodes().stream().collect(Collectors.joining(",", "{", "}")));

	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Evaluates the preceding commands over the given date range.");

	public Evaluate(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	public Table getTable() throws PintoSyntaxException {
		return new Table(compose(), Optional.of(Periodicities.get(parameters.get().getArgument("freq"))
				.range(LocalDate.parse(parameters.get().getArgument("start")),
						LocalDate.parse(parameters.get().getArgument("end")), false)));
	}
}
