package tech.pinto.command.terminal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.pinto.command.CommandHelp;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.data.DoubleData;
import tech.pinto.data.MessageData;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.Outputs;

public class Export extends ParameterizedCommand {

	protected final PeriodicRange<?> range;
	protected final ArrayDeque<Data<?>> output = new ArrayDeque<>();

	public Export(String[] arguments) {
		super("export", AnyData.class, MessageData.class, arguments);
		LocalDate start = LocalDate.parse(arguments[0]);
		LocalDate end = LocalDate.parse(arguments[1]);
		Periodicity<?> p = Periodicities.get(arguments.length > 2 ? arguments[2] : "B");
		this.range = p.range(start, end, false);

		inputCount = Integer.MAX_VALUE;
		outputCount = Integer.MAX_VALUE;
	}

	@Override
	protected void determineOutputCount() {
		outputCount = inputStack.size();
	}

	@Override
	public <P extends Period> MessageData evaluate(PeriodicRange<P> range) {
		output.addLast(inputStack.removeFirst().evaluate(this.range));
		if (output.size() != outputCount) {
			return new MessageData("Adding data column");
		} else {
			Optional<Outputs.StringTable> t = output.stream().map(d -> (Object) d).filter(d -> d instanceof DoubleData)
					.map(d -> (DoubleData) d).collect(Outputs.doubleDataToStringTable());
			if (t.isPresent()) {
				try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arguments[3])))) {
					out.println(Stream.of(t.get().getHeader()).collect(Collectors.joining(",")));
					Stream.of(t.get().getCells())
							.forEach(line -> out.println(Stream.of(line).collect(Collectors.joining(","))));
				} catch (IOException e) {
					throw new IllegalArgumentException("Unable to open file \"" + arguments[3] + "\" for export");
				}
			}

			return new MessageData("Successfully exported");
		}
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
	
	public static Supplier<CommandHelp> getHelp() {
		return () -> new CommandHelp.Builder("export")
				.outputs("none")
				.inputs("any<sub>1</sub>...any<sub>n</sub>")
				.description("Evaluates the preceding commands over the given date range and exports csv for *filename*.")
				.parameter("start date", "prior period", "yyyy-dd-mm")
				.parameter("end date", "prior period", "yyyy-dd-mm")
				.parameter("periodicity", "B", "{B,W-FRI,BM,BQ,BA}")
				.parameter("filename")
				.build();
	}

}
