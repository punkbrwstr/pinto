package tech.pinto.function.functions;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.DoubleStream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Parameters;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.function.ComposableFunction;

public class Resample extends ComposableFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("freq", true, "New perioditicy");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
			.description("Changes periodicity of inputs to *freq*");

	
	public Resample(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer);
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void apply(LinkedList<Column> stack) {
		final Periodicity newPeriodicity  = 
			Periodicities.get(parameters.get().getArgument("freq"));
		LinkedList<Column> inputStack = new LinkedList<>(stack);
		stack.clear();
		for (Column col : inputStack) {
			stack.add(new Column(inputs -> join(inputs[0].toString(), toString()), inputs -> range -> {
				Period newStart = newPeriodicity.roundDown(range.start().endDate());
				if(newStart.endDate().isAfter(range.start().endDate())) {
					newStart = newStart.previous();
				}
				Period newEnd = newPeriodicity.from(range.end().endDate());
				PeriodicRange newDr = newPeriodicity.range(newStart, newEnd, range.clearCache());
				double[] d = ((DoubleStream) inputs[0].getCells(newDr)).toArray();
				DoubleStream.Builder b = DoubleStream.builder();
				range.values().stream().map(Period::endDate).forEach( ed ->
						b.accept(d[(int) newDr.indexOf(newPeriodicity.roundDown(ed))]));
				return b.build();
			}, col));
		}
	}

}
