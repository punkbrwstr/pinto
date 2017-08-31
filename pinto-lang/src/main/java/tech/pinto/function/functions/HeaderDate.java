package tech.pinto.function.functions;


import java.time.LocalDate;

import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;

public class HeaderDate extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("offset", "0", "Number of periods to offset")
			.add("offset_freq", "B", "Periodicity to offset {B,W-FRI,BM,BQ-DEC,BA-DEC}");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Concatenates input headers into one header literal");

	public HeaderDate(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		Periodicity<?> p =  Periodicities.get(parameters.get().getArgument("offset_freq"));
		LocalDate d = p.from(LocalDate.now()).offset(Long.parseLong(parameters.get().getArgument("offset"))).endDate();
		stack.addFirst(new Column(d.toString()));
	}
}
