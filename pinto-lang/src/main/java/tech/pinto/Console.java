package tech.pinto;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Optional;

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
import tech.pinto.tools.Outputs;

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
				Optional<Outputs.StringTable> t = output.stream().filter(d -> d instanceof DoubleData).map(d -> (DoubleData) d)
					.collect(Outputs.doubleDataToStringTable());
				if(t.isPresent()) {
					out.println(FlipTable.of(t.get().getHeader(), t.get().getCells()));
				}
				output.stream().filter(d -> !(d instanceof DoubleData)).map(Object::toString).forEach(out::println);
//			} catch (PintoSyntaxException pse) {
//				System.out.println("Pinto syntax problem: " + pse.getMessage());
//				pse.printStackTrace();
			} catch (Throwable e) {
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
