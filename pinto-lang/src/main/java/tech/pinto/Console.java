package tech.pinto;

import java.io.IOException;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.inject.Singleton;

import com.jakewharton.fliptables.FlipTable;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import tech.pinto.data.Data;
import tech.pinto.data.DoubleData;
import tech.pinto.time.PeriodicRange;

public class Console {

	protected Pinto pinto;

	public Console() {
		ConsoleComponent component = DaggerConsole_ConsoleComponent.builder().consoleModule(new ConsoleModule())
				.build();
		pinto = component.pinto();

	}

	public void run() throws IOException {
		ConsoleReader reader = new ConsoleReader();
		reader.setPrompt("pinto> ");
		reader.addCompleter(new StringsCompleter(pinto.getVocab().getCommands()));
		String line;
		PrintWriter out = new PrintWriter(reader.getOutput());

		while ((line = reader.readLine()) != null) {
			try {
				ArrayDeque<Data<?>> output = (ArrayDeque<Data<?>>) pinto.evaluateStatement(line);
				List<DoubleData> data = new ArrayList<>();
				while (!output.isEmpty() && output.peekFirst() instanceof DoubleData) {
					data.add((DoubleData) output.removeFirst());
				}
				if (data.size() > 0) {
					out.println(formatDoubleData(data));
				} else {
					output.stream().map(Object::toString).forEach(out::println);
				}
			} catch (PintoSyntaxException pse) {
				System.out.println("Pinto syntax problem: " + pse.getMessage());
				pse.printStackTrace();
			} catch (Exception e) {
				System.out.println("Evaluation error: " + e.getCause().getMessage());
				e.printStackTrace();

			}
			out.flush();
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			new Console().run();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				TerminalFactory.get().restore();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String formatDoubleData(List<DoubleData> dd) {
		PeriodicRange<?> range = dd.get(0).getRange();
		List<LocalDate> dates = range.dates();
		String[] labels = Stream.concat(Stream.of("Date"), dd.stream().map(Data::getLabel)).toArray(i -> new String[i]);
		String[][] table = new String[(int) range.size()][dd.size() + 1];
		for (int i = 0; i < range.size(); i++) {
			table[i][0] = dates.get(i).toString();
		}
		for (AtomicInteger i = new AtomicInteger(0); i.get() < dd.size(); i.incrementAndGet()) {
			AtomicInteger j = new AtomicInteger(0);
			dd.get(i.get()).getData().forEach(d -> table[j.getAndIncrement()][i.get() + 1] = Double.toString(d));
		}
		return FlipTable.of(labels, table);

	}

	@Module
	public static class ConsoleModule {
		@Provides
		@Singleton
		Cache provideCache(Vocabulary vocabulary) {
			return new LocalCache(vocabulary);
		}

		@Provides
		@Singleton
		Vocabulary provideVocabulary() {
			return new StandardVocabulary();
		}
	}

	@Component(modules = ConsoleModule.class)
	@Singleton
	public interface ConsoleComponent {
		Pinto pinto();
	}

}
