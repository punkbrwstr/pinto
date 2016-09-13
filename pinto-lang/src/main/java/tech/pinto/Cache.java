package tech.pinto;

import java.util.function.Function;
import java.util.stream.DoubleStream;

import tech.pinto.command.nonedouble.CachedDoubleCommand;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

abstract public class Cache {
	
	
	abstract public String getSaved(String code);
	abstract public void save(String code, String statement);
	abstract public boolean isSaved(String code);
	abstract public String deleteSaved(String code);
	abstract public <P extends Period> DoubleStream evaluateCached(
			CachedDoubleCommand command, PeriodicRange<P> range,
			Function<PeriodicRange<P>,DoubleStream> function);
	

}
