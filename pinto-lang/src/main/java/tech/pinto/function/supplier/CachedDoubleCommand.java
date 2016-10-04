package tech.pinto.function.supplier;

import java.util.LinkedList;
import java.util.List;

import tech.pinto.Cache;
import tech.pinto.TimeSeries;
import tech.pinto.function.Function;
import tech.pinto.function.NullaryReferenceFunction;
import tech.pinto.function.NullarySimpleFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class CachedDoubleCommand extends NullaryReferenceFunction {
	
	protected final Cache cache;
	private int referenceCount = 0;
	
	public CachedDoubleCommand(String name, Cache cache, LinkedList<Function> inputs, String...arguments) {
		super(name, inputs, arguments);
		this.cache = cache;
	}
	
	abstract public <P extends Period> List<TimeSeries> evaluateAllUncached(PeriodicRange<P> range);

	protected <P extends Period> TimeSeries evaluateOne(int i, PeriodicRange<P> range) {
		return cache.evaluateCached(toString(), myOutputCount(), range, r -> evaluateAllUncached(r)).get(i);
	}
	
	@Override
	protected Function supplyReference() {
		final int i = referenceCount++;
		return new NullarySimpleFunction(toString() + "[" + i + "]",range -> this.evaluateOne(i, range));
	}

}
