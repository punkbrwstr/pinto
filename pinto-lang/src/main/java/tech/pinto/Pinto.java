package tech.pinto;

import java.util.ArrayDeque;

import javax.inject.Inject;

import tech.pinto.command.Command;
import tech.pinto.command.anyany.Statement;
import tech.pinto.data.Data;

public class Pinto {

	@Inject Cache cache;
	@Inject Vocabulary vocab;

    @Inject
	public Pinto() {
	}
	
	public ArrayDeque<Data<?>> evaluateStatement(String statement) throws Exception {
		try {
			ArrayDeque<Data<?>> output = new ArrayDeque<>();
			for(Command terminal : new Statement(cache, vocab, statement).getTerminalCommands()) {
				for(int i = 0; i < terminal.outputCount(); i++) {
					System.out.println(terminal.summarize(""));
					output.addLast(terminal.evaluate(null));
				}
			}
			return output;
		} catch(RuntimeException e) {
			throw new Exception(e);
		}
	}

	public Cache getCache() {
		return cache;
	}

	public Vocabulary getVocab() {
		return vocab;
	}
	
	

}
