package tech.pinto.extras.command.nonedouble;

import java.util.ArrayDeque;

import tech.pinto.Cache;
import tech.pinto.command.nonedouble.CachedDoubleCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.extras.BloombergClient;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Bloomberg extends CachedDoubleCommand {

	public Bloomberg(BloombergClient bc, Cache cache, String... arguments) {
		super("bbg", cache, arguments);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected <P extends Period> ArrayDeque<DoubleData> evaluate(PeriodicRange<P> range) {
		// TODO Auto-generated method stub
		return null;
	}

}
