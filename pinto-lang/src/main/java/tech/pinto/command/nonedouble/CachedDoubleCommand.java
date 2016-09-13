package tech.pinto.command.nonedouble;

import java.util.ArrayDeque;


import java.util.stream.DoubleStream;

import tech.pinto.Cache;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.DoubleData;
import tech.pinto.data.NoneData;
import tech.pinto.time.PeriodicRange;

abstract public class CachedDoubleCommand extends ParameterizedCommand<Object,NoneData,DoubleStream,DoubleData> {
	
	protected final Cache cache;
	
	public CachedDoubleCommand(String name, Cache cache, String...arguments) {
		super(name, NoneData.class, DoubleData.class, arguments);
		inputCount = 0;
		outputCount = 1;
		this.cache = cache;
	}


    public ArrayDeque<DoubleData> getOutputData(PeriodicRange<?> range) {
        if(outputStack == null) {
        	outputStack = new ArrayDeque<>();
            outputStack.addFirst(new DoubleData(range,toString(),
            		cache.evaluateCached(this, range, r -> evaluate(r).getFirst().getData())));
        }
        return outputStack;
    }

}
