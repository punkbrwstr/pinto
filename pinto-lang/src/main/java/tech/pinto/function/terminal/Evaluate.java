package tech.pinto.function.terminal;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.TimeSeries;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

public class Evaluate extends TerminalFunction {

	
	public Evaluate(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, namespace, previousFunction, indexer, args);
	}

	@Override
	public Optional<LinkedList<TimeSeries>> getTimeSeries() throws PintoSyntaxException {
		Periodicity<?> p =  Periodicities.get(args.length > 2 ? args[2] : "B");
		LocalDate start = args.length > 0 ? LocalDate.parse(args[0]) : 
							p.from(LocalDate.now()).previous().endDate();
		LocalDate end = args.length > 1 ? LocalDate.parse(args[1]) : 
							p.from(LocalDate.now()).previous().endDate();
		return Optional.of(indexer.index(this.previousFunction.get().compose()).stream()
				.map(f -> f.evaluate(p.range(start, end, false)))
					.collect(Collectors.toCollection(() -> new LinkedList<>())));
	}
	
	public static FunctionHelp getHelp(String name) {
		return  new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Evaluates the preceding commands over the given date range.")
				.parameter("start date", "prior period", "yyyy-dd-mm")
				.parameter("end date", "prior period", "yyyy-dd-mm")
				.parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}")
				.build();
	}

}
