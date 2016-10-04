package tech.pinto.function;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class TerminalFunction extends Function {
	
	protected Optional<List<TimeSeries>> timeSeriesOutput = Optional.empty();
	protected Optional<String> message = Optional.empty();

	public TerminalFunction(String name, LinkedList<Function> inputStack, String... arguments) {
		super(name, inputStack, arguments);
		outputCount = 0;
	}
	
	public Optional<List<TimeSeries>> getTimeSeries() {
		return timeSeriesOutput;
	}

	public Optional<String> getText() {
		return message;
	}

	@Override
	public Function getReference() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Function clone() {
		throw new UnsupportedOperationException();
	}


}
