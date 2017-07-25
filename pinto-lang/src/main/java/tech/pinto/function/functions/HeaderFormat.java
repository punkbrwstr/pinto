package tech.pinto.function.functions;


import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Optional;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ParameterType;
import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.function.ComposableFunction;

public class HeaderFormat extends ComposableFunction {
	
	
	public HeaderFormat(String name, ComposableFunction previousFunction, Indexer indexer) {
		super(name,previousFunction, indexer, ParameterType.arguments_required);
	}
	
	protected String getHeaderFormat() {
		String format = getArgs()[0];
		format = format.replaceAll("\\{\\}", "\\{0\\}");
		return format;
	}

	@Override
	protected LinkedList<Column> apply(LinkedList<Column> stack) {
			MessageFormat mf = new MessageFormat(getHeaderFormat());
			ArrayDeque<Column> temp = new ArrayDeque<>();
			while(!stack.isEmpty()) {
				Column old = stack.removeFirst();
				temp.addFirst(new Column(old.getInputs(),Optional.of(inputs -> mf.format(new Object[] {old.toString()})), old.getSeriesFunction()));
			}
			temp.stream().forEach(stack::addFirst);
		return stack;
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("n")
				.description("Formats labels according to supplied format string.")
				.parameter("format string ({0} for existing label)")
				.build();
	}

	

}
