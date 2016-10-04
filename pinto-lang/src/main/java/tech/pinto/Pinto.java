package tech.pinto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;
import tech.pinto.function.intermediate.Expression;

public class Pinto {

	@Inject
	Cache cache;
	@Inject
	Vocabulary vocab;

	private final LinkedList<Function> stack = new LinkedList<>();

	@Inject
	public Pinto() {
	}

	public List<Response> execute(String statement) throws Exception {
		try {
			List<Response> output = new ArrayList<>();
			Expression s = new Expression(cache, vocab, statement, stack);
			while(!s.getTerminalCommands().isEmpty()) {
				TerminalFunction terminal = s.getTerminalCommands().removeLast();
				output.add(new Response(terminal.getTimeSeries(),terminal.getText()));
			}
			
			stack.addAll(s.getStack());
			return output;
		} catch (RuntimeException e) {
			throw new Exception(e);
		}
	}

	public Cache getCache() {
		return cache;
	}

	public Vocabulary getVocab() {
		return vocab;
	}
	
	public static class Response {
		
		private final Optional<List<TimeSeries>> timeseriesOutput;
		private final Optional<String> messageOutput;

		public Response(Optional<List<TimeSeries>> timeseriesOutput, Optional<String> messageOutput) {
			this.timeseriesOutput = timeseriesOutput;
			this.messageOutput = messageOutput;
		}

		public Optional<List<TimeSeries>> getTimeseriesOutput() {
			return timeseriesOutput;
		}

		public Optional<String> getMessageOutput() {
			return messageOutput;
		}
		
	}

}
