package tech.pinto.command;

import tech.pinto.Cache;

@FunctionalInterface
public interface CommandFactory {
	
	public Command build(Cache cache, String...arguments);

}
