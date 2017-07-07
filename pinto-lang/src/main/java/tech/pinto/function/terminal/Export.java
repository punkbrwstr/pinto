package tech.pinto.function.terminal;

import java.io.BufferedWriter;



import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.ColumnValues;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.Outputs;

public class Export extends TerminalFunction {

	public Export(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, namespace, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<ColumnValues> getColumnValues() throws PintoSyntaxException {
		if(args.length < 4) {
			throw new PintoSyntaxException(name + " requires 4 arguments.");
		}
		Periodicity<?> p =  Periodicities.get(args.length > 2 ? args[2] : "B");
		LocalDate start = args.length > 0 ? LocalDate.parse(args[0]) : 
							p.from(LocalDate.now()).previous().endDate();
		LocalDate end = args.length > 1 ? LocalDate.parse(args[1]) : 
							p.from(LocalDate.now()).previous().endDate();
		PeriodicRange<?> range = p.range(start, end, false);
		Outputs.StringTable t = this.previousFunction.get().compose().stream()
				.map(f -> f.getValues(range)).collect(Outputs.columnValuesCollector(range));
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[3])))) {
			out.println(Stream.of(t.getHeader()).collect(Collectors.joining(",")));
			Stream.of(t.getCells())
						.forEach(line -> out.println(Stream.of(line).collect(Collectors.joining(","))));
		} catch (IOException e) {
				throw new IllegalArgumentException("Unable to open file \"" + args[3] + "\" for export");
		}
		return createTextColumn("Successfully exported.");
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("none")
				.description("Evaluates the preceding commands over the given date range and exports csv for *filename*.")
				.parameter("start date", "prior period", "yyyy-dd-mm")
				.parameter("end date", "prior period", "yyyy-dd-mm")
				.parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}")
				.parameter("filename")
				.build();
	}


}
