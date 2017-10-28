package tech.pinto.function.functions.terminal;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Parameters;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Periodicities;
import tech.pinto.function.ComposableFunction;

public class Export extends Evaluate {
	protected static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("filename", true, "File name for csv output")
			.add("start", LocalDate.now().toString(), "Start date of range to evaluate (format: yyyy-mm-dd)")
			.add("end", LocalDate.now().toString(), "End date of range to evaluate (format: yyyy-mm-dd)")
			.add("freq", "B", "Periodicity of range to evaluate " +
						Periodicities.allCodes().stream().collect(Collectors.joining(",", "{", "}")));
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
				.description("Evaluates the preceding expression over the given date range exports csv *filename*");

	public Export(String name, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	public Table getTable() throws PintoSyntaxException {
		String filename = parameters.get().getArgument("filename");
		Table t = super.getTable();
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
