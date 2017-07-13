package tech.pinto.function.terminal;

import java.time.LocalDate;


import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.Column;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

public class Evaluate extends TerminalFunction {

	public Evaluate(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
	}

	public Table getTable() throws PintoSyntaxException {
		LinkedList<Column> stack = compose();
		Periodicity<?> p =  Periodicities.get(getArgs().length > 2 ? getArgs()[2] : "B");
		LocalDate start = getArgs().length > 0 && !getArgs()[0].equals("") ? LocalDate.parse(getArgs()[0]) : 
						p.from(LocalDate.now()).endDate();
		LocalDate end = getArgs().length > 1 ? LocalDate.parse(getArgs()[1]) : 
						p.from(LocalDate.now()).endDate();
		return new Table(stack, Optional.of(p.range(start, end, false)));
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
