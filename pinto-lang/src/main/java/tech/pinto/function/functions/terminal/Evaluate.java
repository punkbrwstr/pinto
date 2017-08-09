package tech.pinto.function.functions.terminal;

import java.time.LocalDate;


import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Parameters;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.Column;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

public class Evaluate extends TerminalFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("start", false, "Start date of range to evaluate (format: yyyy-mm-dd)")
			.add("end", false, "End date of range to evaluate (format: yyyy-mm-dd)")
			.add("freq", "B", "Periodicity of range to evaluate {B,W-FRI,BM,BQ,BA}");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Evaluates the preceding commands over the given date range.");

	public Evaluate(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	public Table getTable() throws PintoSyntaxException {
		LinkedList<Column> stack = compose();
		Periodicity<?> p =  Periodicities.get(parameters.get().getArgument("freq"));
		LocalDate start =  parameters.get().hasArgument("start") ?
				LocalDate.parse(parameters.get().getArgument("start")) : p.from(LocalDate.now()).endDate();
		LocalDate end =  parameters.get().hasArgument("end") ?
				LocalDate.parse(parameters.get().getArgument("end")) : p.from(LocalDate.now()).endDate();
		return new Table(stack, Optional.of(p.range(start, end, false)));
	}
}
