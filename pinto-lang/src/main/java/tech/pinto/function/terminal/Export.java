package tech.pinto.function.terminal;

import java.io.BufferedWriter;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.Outputs;

public class Export extends TerminalFunction {


	public Export(String name, LinkedList<Function> inputs, String[] arguments) {
		super(name, inputs, arguments);
		LocalDate start = LocalDate.parse(arguments[0]);
		LocalDate end = LocalDate.parse(arguments[1]);
		Periodicity<?> p = Periodicities.get(arguments.length > 2 ? arguments[2] : "B");
		PeriodicRange<?> range = p.range(start, end, false);
		List<TimeSeries> output = inputStack.stream().map(f -> f.evaluate(range)).collect(Collectors.toList());
		Optional<Outputs.StringTable> t = output.stream().map(d -> (Object) d).filter(d -> d instanceof TimeSeries)
					.map(d -> (TimeSeries) d).collect(Outputs.doubleDataToStringTable());
		if (t.isPresent()) {
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arguments[3])))) {
				out.println(Stream.of(t.get().getHeader()).collect(Collectors.joining(",")));
				Stream.of(t.get().getCells())
						.forEach(line -> out.println(Stream.of(line).collect(Collectors.joining(","))));
			} catch (IOException e) {
				throw new IllegalArgumentException("Unable to open file \"" + arguments[3] + "\" for export");
			}
		}

		message = Optional.of("Successfully exported");

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
