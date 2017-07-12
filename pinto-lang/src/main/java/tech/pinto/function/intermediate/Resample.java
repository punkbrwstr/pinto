package tech.pinto.function.intermediate;

import java.util.LinkedList;

import java.util.stream.DoubleStream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.function.ComposableFunction;

public class Resample extends ComposableFunction {

	
	public Resample(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.parameter("periodicity")
				.description("Changes periodicity of inputs to *periodicity*.")
				.build();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected LinkedList<Column> compose(LinkedList<Column> stack) {
		if(getArgs().length == 0) {
			throw new IllegalArgumentException(name + " requires a periodicity as an argument.");
		} else if(!Periodicities.allCodes().contains(getArgs()[0])) {
			throw new IllegalArgumentException("Invalid periodicity \"" + getArgs()[0] + "\" for " + name + ".");
		}
		final Periodicity newPeriodicity  = Periodicities.get(getArgs()[0]);
		LinkedList<Column> outputs = new LinkedList<>();
		for (Column function : stack) {
			outputs.add(new Column(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				Period newStart = newPeriodicity.roundDown(range.start().endDate());
				if(newStart.endDate().isAfter(range.start().endDate())) {
					newStart = newStart.previous();
				}
				Period newEnd = newPeriodicity.from(range.end().endDate());
				PeriodicRange newDr = newPeriodicity.range(newStart, newEnd, range.clearCache());
				double[] d = inputs[0].getValues(newDr).getSeries().get().toArray();
				DoubleStream.Builder b = DoubleStream.builder();
				range.values().stream().map(Period::endDate).forEach( ed ->
						b.accept(d[(int) newDr.indexOf(newPeriodicity.roundDown(ed))]));
				return b.build();
			}, function));
		}
		return outputs;
	}

}
