package tech.pinto.function;

import java.util.LinkedList;
import java.util.List;

import tech.pinto.Namespace;

@FunctionalInterface
public interface FunctionFactory {
	
	public Function build(String name, Namespace namespace, LinkedList<Function> inputs,
								List<String> saveString, String...arguments);

}
