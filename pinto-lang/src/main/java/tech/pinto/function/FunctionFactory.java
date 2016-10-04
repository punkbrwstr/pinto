package tech.pinto.function;

import java.util.LinkedList;
import java.util.List;

import tech.pinto.Cache;

@FunctionalInterface
public interface FunctionFactory {
	
	public Function build(Cache cache, LinkedList<Function> inputs,
								List<String> saveString, String...arguments);

}
