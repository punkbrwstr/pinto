package pinto;

import java.util.function.Function;
import java.util.stream.DoubleStream;

import pinto.command.nonedouble.CachedDoubleCommand;
import pinto.time.Period;
import pinto.time.PeriodicRange;

abstract public class Cache {
	
	protected final Vocabulary vocabulary;
	
	public Cache(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}
	
	
	abstract public String getSaved(String code);
	abstract public void save(String code, String statement);
	abstract public boolean isSaved(String code);
	abstract public String deleteSaved(String code);
	abstract public <P extends Period> DoubleStream evaluateCached(
			CachedDoubleCommand command, PeriodicRange<P> range,
			Function<PeriodicRange<P>,DoubleStream> function);
	
	


}
