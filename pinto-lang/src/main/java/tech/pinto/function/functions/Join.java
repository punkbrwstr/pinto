package tech.pinto.function.functions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.function.FunctionHelp;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Join extends ComposableFunction {

	public Join(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
		return asList(new Column(inputs -> toString(), inputs -> range -> evaluationFunction(range, inputs),
				stack.toArray(new Column[] {})));
	}

	private <P extends Period> DoubleStream evaluationFunction(PeriodicRange<P> range, Column[] inputArray) {
		LinkedList<Column> inputs = asList(inputArray);
		Collections.reverse(inputs);
		List<LocalDate> cutoverDates = Stream.of(getArgs()).map(LocalDate::parse).collect(Collectors.toList());
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

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name).outputs("1").description("Fills missing data with last good obseration.")
				.parameter("date<sub>1</sub>, date<sub>z</sub>", null, "yyyy-mm-dd").build();
	}

}
