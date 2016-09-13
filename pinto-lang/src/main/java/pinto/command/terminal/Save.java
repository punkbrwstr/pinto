package pinto.command.terminal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.stream.Collectors;

import pinto.Cache;
import pinto.command.ParameterizedCommand;
import pinto.data.AnyData;
import pinto.data.MessageData;
import pinto.time.Period;
import pinto.time.PeriodicRange;

public class Save extends ParameterizedCommand<Object, AnyData, String, MessageData> {

	private final String code;
	private final Cache cache;
	private String formula;
	
	public Save(Cache cache, String[] arguments) {
		super("save", AnyData.class, MessageData.class, arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("save requires one argument.");
		}
		this.cache = cache;
		code = arguments[0];
		inputCount = Integer.MAX_VALUE;
	}
	
	public void setFormula(String formula) {
		this.formula = formula;
	}

	@Override
	protected <P extends Period> ArrayDeque<MessageData> evaluate(PeriodicRange<P> range) {
//		ArrayList<String> fullStatement = new ArrayList<>();
//		while(!inputStack.isEmpty()) {
//			fullStatement.add(inputStack.removeLast().summarize());
//		}
//		cache.save(code, fullStatement.stream().collect(Collectors.joining(" ")));
		cache.save(code, formula);
		ArrayDeque<MessageData> out = new ArrayDeque<>();
		out.addFirst(new MessageData("Successfully saved."));
		return out;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	
	

}
