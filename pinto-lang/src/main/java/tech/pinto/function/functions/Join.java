package tech.pinto.function.functions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.ComposableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Join extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("dates", true, "Cutover dates separated by commas (yyyy-mm-dd)");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Joins series a given cutover dates");

	public Join(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	protected void apply(LinkedList<Column> stack) {
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		stack.add(new Column(inputs -> toString(), inputs -> range -> evaluationFunction(range, inputs),
				inputStack.toArray(new Column[] {})));
	}

	private <P extends Period> DoubleStream evaluationFunction(PeriodicRange<P> range, Column[] inputArray) {
		LinkedList<Column> inputs = asList(inputArray);
		String[] datesStrings = parameters.get().getArgument("dates").split(",");
		List<LocalDate> cutoverDates = Stream.of(datesStrings).map(String::trim).map(LocalDate::parse).collect(Collectors.toList());
		Collections.reverse(inputs);
		DoubleStream ds = DoubleStream.empty().sequential();
		List<P> cutoverPeriods = cutoverDates.stream().map(range.periodicity()::from).collect(Collectors.toList());
		Period current = range.start();
		int i = 0;

		while (i < cutoverPeriods.size() && !current.isAfter(range.end())) {
			Column currentFunction = inputs.removeFirst();
			if (current.isBefore(cutoverPeriods.get(i))) {
				P chunkEnd = range.end().isBefore(cutoverPeriods.get(i)) ? range.end()
						: range.periodicity().previous(cutoverPeriods.get(i));
				PeriodicRange<?> chunkRange = range.periodicity().range(current, chunkEnd, range.clearCache());
				ds = DoubleStream.concat(ds, currentFunction.getCells(chunkRange));
				current = range.periodicity().next(chunkEnd);
			}
			i++;
		}

		if (inputs.isEmpty()) {
			throw new IllegalArgumentException("Not enough inputs for " + toString());
		}
		Column currentFunction = inputs.removeFirst();
		if (!current.isAfter(range.end())) {
			PeriodicRange<?> chunkRange = range.periodicity().range(current, range.end(), range.clearCache());
			ds = DoubleStream.concat(ds, currentFunction.getCells(chunkRange));
		}
		return ds;

	}
}
