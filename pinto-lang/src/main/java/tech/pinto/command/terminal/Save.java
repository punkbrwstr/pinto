package tech.pinto.command.terminal;

import java.util.function.Supplier;

import tech.pinto.Cache;
import tech.pinto.command.CommandHelp;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.MessageData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Save extends ParameterizedCommand {

	private final String code;
	private final Cache cache;
	private String formula;
	
	public Save(Cache cache, String[] arguments) {
		super("def", AnyData.class, MessageData.class, arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("save requires one argument.");
		}
		this.cache = cache;
		code = arguments[0];
		inputCount = Integer.MAX_VALUE;
		outputCount = 1;
	}
	
	public void setFormula(String formula) {
		this.formula = formula;
	}

	@Override
	public <P extends Period> MessageData evaluate(PeriodicRange<P> range) {
//		ArrayList<String> fullStatement = new ArrayList<>();
//		while(!inputStack.isEmpty()) {
//			fullStatement.add(inputStack.removeLast().summarize());
//		}
//		cache.save(code, fullStatement.stream().collect(Collectors.joining(" ")));
		cache.save(code, formula);
		return new MessageData("Successfully saved.");
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	public static Supplier<CommandHelp> getHelp() {
		return () -> new CommandHelp.Builder("def")
				.inputs("any<sub>1</sub>...any<sub>n</sub>")
				.outputs("none")
				.description("Defines the preceding commands as a new command, named *name*.")
				.parameter("name")
				.build();
	}
	
	
	

}
