package tech.pinto.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.pinto.data.Data;
import tech.pinto.data.DoubleData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class EvaluateMultipleAtOnceCommand extends ParameterizedCommand {

	private Map<PeriodicRange<?>,List<DoubleData>> data = new HashMap<>();
	private int referenceCount = 0;

	public EvaluateMultipleAtOnceCommand(String name, Class<? extends Data<?>> inputType,
			Class<? extends Data<?>> outputType, String... arguments) {
		super(name, inputType, outputType, arguments);
	}

	abstract protected <P extends Period> List<DoubleData> evaluateAll(PeriodicRange<P> range);

	public <P extends Period> DoubleData evaluateOne(int i, PeriodicRange<P> range) {
		List<DoubleData> d = data.containsKey(range) ? data.get(range) : evaluateAll(range);
		return d.get(i);
	}

	@Override public Command getReference() {
		final int i = referenceCount++;
		return new SimpleCommand(this,1,1,range -> this.evaluateOne(i, range));
	}


	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		throw new UnsupportedOperationException();
	}

}
