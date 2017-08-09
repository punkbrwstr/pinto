package tech.pinto.function.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Period;

public class ImportCSV extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("source", true, "URL or file path for csv")
			.add("includes_header", "true", "Whether or not first row contains headers");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Imports table from a csv formatted file or URL");

	public ImportCSV(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		String source = parameters.get().getArgument("source");
		boolean includesHeader = Boolean.parseBoolean(parameters.get().getArgument("includes_header"));
		try {
			List<String> lines = null;
			if(!source.contains("http")) {
				lines = Files.readAllLines(Paths.get(source));
			} else {
				lines = new ArrayList<>();
		        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(source).openStream()));
		        String inputLine;
		        while ((inputLine = in.readLine()) != null)
		            lines.add(inputLine);
		        in.close();
			}
			if (lines.size() > 0) {
				String[] firstRow = lines.get(0).split(",");
				final String[] labels = includesHeader ? Arrays.copyOfRange(firstRow, 1, firstRow.length)
						: getColumnLetters(firstRow.length);
				final Map<LocalDate, String[]> data = lines.stream().skip(includesHeader ? 1 : 0).map(s -> s.split(","))
						.collect(Collectors.toMap((r) -> LocalDate.parse(r[0]), Function.identity()));
				for(int i = 0; i < firstRow.length - 1; i++) {
					final int col = i;
					stack.add(new Column(inputs -> labels[col], 
							inputs -> range -> {
							DoubleStream.Builder b = DoubleStream.builder();
							for (Period per : range.values()) {
								if (data.containsKey(per.endDate())) {
									try {
										b.accept(Double.valueOf(data.get(per.endDate())[col + 1]));
									} catch(NumberFormatException nfe) {
										b.accept(Double.NaN);
									}
								} else {
									b.accept(Double.NaN);
								}
							}
							return b.build();
								
					}));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to import file \"" + source + "\".", e);
		}
		// IntStream.range(0,count).mapToDouble(i -> (double)i).mapToObj(
		// value -> new EvaluableFunction(inputs -> Double.toString(value),
		// inputs -> range -> DoubleStream.iterate(value, r ->
		// value).limit(range.size()))).forEach(stack::add);;
	}

	private static String[] getColumnLetters(int columnCount) {
		String[] columnStrings = new String[columnCount];
		for (int i = 0; i < columnCount; i++) {
			String columnString = "";
			float columnNumber = i;
			while (columnNumber > 0) {
				float currentLetterNumber = (columnNumber - 1) % 26;
				char currentLetter = (char) (currentLetterNumber + 65);
				columnString = currentLetter + columnString;
				columnNumber = (columnNumber - (currentLetterNumber + 1)) / 26;
			}
			columnStrings[i] = columnString;

		}
		return columnStrings;
	}
}
