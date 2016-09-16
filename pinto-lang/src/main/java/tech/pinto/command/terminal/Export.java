package tech.pinto.command.terminal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.DoubleData;
import tech.pinto.data.MessageData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.Outputs;

public class Export extends ParameterizedCommand<Object, AnyData, String, MessageData> {

	public Export(String[] arguments) {
		super("export", AnyData.class, MessageData.class, arguments);
		inputCount = Integer.MAX_VALUE;
	}

	@Override
	protected <P extends Period> ArrayDeque<MessageData> evaluate(PeriodicRange<P> range) {
		// passed range is null
		LocalDate start = LocalDate.parse(arguments[0]);
		LocalDate end = LocalDate.parse(arguments[1]);
		Periodicity<?> p =  Periodicities.get(arguments.length > 2 ? arguments[2] : "B");
		PeriodicRange<?> r = p.range(start, end, false);
		ArrayDeque<MessageData> result = new ArrayDeque<>();
		@SuppressWarnings("rawtypes")
		ArrayDeque<?> ad = inputStack.stream().flatMap(c -> c.getOutputData(r).stream())
								.collect(Collectors.toCollection(() -> new ArrayDeque()));
		Optional<Outputs.StringTable> t = ad.stream().filter(d -> d instanceof DoubleData).map(d -> (DoubleData) d)
					.collect(Outputs.doubleDataToStringTable());
		if(t.isPresent()) {
			try (PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(arguments[3])))) {
				out.println(Stream.of(t.get().getHeader()).collect(Collectors.joining(",")));
				Stream.of(t.get().getCells()).forEach(line -> out.println(Stream.of(line).collect(Collectors.joining(","))));
			} catch (IOException e) {
				throw new IllegalArgumentException("Unable to open file \"" + arguments[3] + "\" for export");
			}
			result.addFirst(new MessageData("Successfully exported"));
		} else {
			result.addFirst(new MessageData("Nothing to export"));
		}

		return result;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	

}
