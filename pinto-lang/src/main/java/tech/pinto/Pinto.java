package tech.pinto;

import java.util.ArrayDeque;

import javax.inject.Inject;

import tech.pinto.command.anyany.Statement;
import tech.pinto.data.Data;

public class Pinto {

	@Inject Cache cache;
	@Inject Vocabulary vocab;

    @Inject
	public Pinto() {
	}
	
	public ArrayDeque<? extends Data<?>> evaluateStatement(String statement) throws Exception {
		try {
			return new Statement(cache, vocab, statement).evaluate(null);
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
