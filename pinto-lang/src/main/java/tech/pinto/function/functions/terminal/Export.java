package tech.pinto.function.functions.terminal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Parameters;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

public class Export extends TerminalFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("filename", true, "File name for csv output")
			.add("start", false, "Start date of range to evaluate (format: yyyy-mm-dd)")
			.add("end", false, "End date of range to evaluate (format: yyyy-mm-dd)")
			.add("freq", "B", "Periodicity of range to evaluate {B,W-FRI,BM,BQ,BA}");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
				.description("Evaluates the preceding commands over the given date range and exports csv for *filename*");

	public Export(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
	}

	@Override
	public Table getTable() throws PintoSyntaxException {
		LinkedList<Column> stack = compose();
		String filename = parameters.get().getArgument("filename");
		Periodicity<?> p =  Periodicities.get(parameters.get().getArgument("freq"));
		LocalDate start =  parameters.get().hasArgument("start") ?
				LocalDate.parse(parameters.get().getArgument("start")) : p.from(LocalDate.now()).endDate();
		LocalDate end =  parameters.get().hasArgument("end") ?
				LocalDate.parse(parameters.get().getArgument("end")) : p.from(LocalDate.now()).endDate();
		Table t = new Table(stack, Optional.of(p.range(start, end, false)));
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
			out.println(Stream.of(t.headerToText()).collect(Collectors.joining(",")));
			Stream.of(t.seriesToText(NumberFormat.getInstance()))
						.forEach(line -> out.println(Stream.of(line).collect(Collectors.joining(","))));
		} catch (IOException e) {
				throw new IllegalArgumentException("Unable to open file \"" + filename + "\" for export");
		}
		return createTextColumn("Successfully exported.");
	}
}
