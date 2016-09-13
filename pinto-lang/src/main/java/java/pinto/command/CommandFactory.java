package pinto.command;

import pinto.Cache;

@FunctionalInterface
public interface CommandFactory {
	
	public Command<?,?,?,?> build(Cache cache, String...arguments);

}
