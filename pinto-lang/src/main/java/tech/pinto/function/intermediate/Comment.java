package tech.pinto.function.intermediate;

import java.util.LinkedList;

import tech.pinto.function.Function;
import tech.pinto.function.NullaryReferenceFunction;

public class Comment extends NullaryReferenceFunction {

	public Comment(LinkedList<Function> inputs) {
		super("comment", inputs, new String[]{});
		// does nothing
	}

	@Override
	protected Function supplyReference() { throw new UnsupportedOperationException(); }

	@Override
	protected int myOutputCount() {
		return 0;
	}

}
