package pinto;

import java.util.ArrayDeque;

import pinto.command.anyany.Statement;
import pinto.data.Data;

public class Pinto {

	private final Cache cache;
	private final Vocabulary vocab;

	public Pinto(Cache cache, Vocabulary vocab) {
		this.cache = cache;
		this.vocab = vocab;
	}
	
	public ArrayDeque<? extends Data<?>> evaluateStatement(String statement) throws Exception {
		try {
			return new Statement(cache, vocab, statement).evaluate(null);
		} catch(RuntimeException e) {
			throw new Exception(e);
		}
		
	}

}
