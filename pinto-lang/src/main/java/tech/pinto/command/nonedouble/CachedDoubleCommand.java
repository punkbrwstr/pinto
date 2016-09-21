package tech.pinto.command.nonedouble;

import java.util.List;

import tech.pinto.Cache;
import tech.pinto.command.EvaluateMultipleAtOnceCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.data.NoneData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class CachedDoubleCommand extends EvaluateMultipleAtOnceCommand {
	
	protected final Cache cache;
	
	public CachedDoubleCommand(String name, Cache cache, String...arguments) {
		super(name, NoneData.class, DoubleData.class, arguments);
		inputCount = 0;
		this.cache = cache;
	}
	
	abstract public <P extends Period> List<DoubleData> evaluateAllUncached(PeriodicRange<P> range);

	@Override
	protected <P extends Period> List<DoubleData> evaluateAll(PeriodicRange<P> range) {
		return cache.evaluateCached(toString(), outputCount, range, r -> evaluateAllUncached(r));
	}


}
