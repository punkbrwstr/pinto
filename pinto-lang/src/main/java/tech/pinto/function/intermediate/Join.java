package tech.pinto.function.intermediate;

import java.time.LocalDate;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.EvaluableFunction;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Join extends ComposableFunction {

	public Join(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, previousFunction, indexer, args);
	}

	@Override
	public LinkedList<EvaluableFunction> composeIndexed(LinkedList<EvaluableFunction> stack) {
		return asList(new EvaluableFunction(inputs -> toString(), inputs -> range -> evaluationFunction(range, inputs),
				stack.toArray(new EvaluableFunction[] {})));
	}

	private <P extends Period> DoubleStream evaluationFunction(PeriodicRange<P> range, EvaluableFunction[] inputArray) {
		LinkedList<EvaluableFunction> inputs = asList(inputArray);
		List<LocalDate> cutoverDates = Stream.of(args).map(LocalDate::parse).collect(Collectors.toList());
		DoubleStream ds = DoubleStream.empty().sequential();
		List<P> cutoverPeriods = cutoverDates.stream().map(range.periodicity()::from).collect(Collectors.toList());
		Period current = range.start();
		int i = 0;

		while (i < cutoverPeriods.size() && !current.isAfter(range.end())) {
			EvaluableFunction currentFunction = inputs.removeFirst();
			if (current.isBefore(cutoverPeriods.get(i))) {
				P chunkEnd = range.end().isBefore(cutoverPeriods.get(i)) ? range.end()
						: range.periodicity().previous(cutoverPeriods.get(i));
				PeriodicRange<?> chunkRange = range.periodicity().range(current, chunkEnd, range.clearCache());
				ds = DoubleStream.concat(ds, currentFunction.evaluate(chunkRange).stream());
				current = range.periodicity().next(chunkEnd);
			}
			i++;
		}

		if (inputs.isEmpty()) {
			throw new IllegalArgumentException("Not enough inputs for " + toString());
		}
		EvaluableFunction currentFunction = inputs.removeFirst();
		if (!current.isAfter(range.end())) {
			PeriodicRange<?> chunkRange = range.periodicity().range(current, range.end(), range.clearCache());
			ds = DoubleStream.concat(ds, currentFunction.evaluate(chunkRange).stream());
		}
		return ds;

	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name).outputs("1").description("Fills missing data with last good obseration.")
				.parameter("date<sub>1</sub>, date<sub>z</sub>", null, "yyyy-mm-dd").build();
	}

}
