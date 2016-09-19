package tech.pinto.command.terminal;

import tech.pinto.Cache;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.MessageData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Delete extends ParameterizedCommand {

	private final Cache cache;
	
	public Delete(Cache cache, String[] arguments) {
		super("save", AnyData.class, MessageData.class, arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("del requires one argument.");
		}
		this.cache = cache;
		inputCount = 0;
		outputCount = 1;
	}
	

	@Override
	public <P extends Period> MessageData evaluate(PeriodicRange<P> range) {
		cache.deleteSaved(arguments[0]);
		return new MessageData("Successfully saved.");
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	
	

}
