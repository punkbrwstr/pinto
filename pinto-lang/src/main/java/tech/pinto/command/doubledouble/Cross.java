package tech.pinto.command.doubledouble;

import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import tech.pinto.command.CommandHelp;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;


public class Cross extends ParameterizedCommand {
	
	private final Supplier<DoubleCollector> collectorSupplier;
	
	public Cross(String name, Supplier<DoubleCollector> collectorSupplier, String... args) {
		super(name, DoubleData.class, DoubleData.class, args);
		this.collectorSupplier = collectorSupplier;
		if(args.length > 0) {
			inputCount = Integer.parseInt(args[0]);
		}
		outputCount = 1;
	}

	
	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		Builder b = DoubleStream.builder();
		List<OfDouble> l = inputStack.stream().map(c -> evaluate(range))
					.map(DoubleData::getData).map(ds -> ds.iterator()).collect(Collectors.toList());
		for(int i = 0; i < range.size(); i++) {
			DoubleCollector dc = collectorSupplier.get();
			l.forEach(di -> dc.add(di.nextDouble()));
			b.accept(dc.finish());
		}
		return new DoubleData(range, toString(), b.build());
	}
	
	public static Supplier<CommandHelp> getHelp(String name, String description) {
		return () -> new CommandHelp.Builder(name)
				.inputs("double<sub>1</sub>...double<sub>n</sub>")
				.outputs("double")
				.description("Calculates " + description + " across *n* inputs.")
				.parameter("n","all",null)
				.build();
	}

}
