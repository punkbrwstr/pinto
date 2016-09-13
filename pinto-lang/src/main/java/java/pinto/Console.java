package pinto;

import java.io.IOException;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.jakewharton.fliptables.FlipTable;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import pinto.data.Data;
import pinto.data.DoubleData;
import pinto.time.PeriodicRange;

public class Console {

	public static void main(String[] args) throws IOException {
		try {
			Vocabulary vocab = new PintoVocabulary();
			Pinto pinto = new Pinto(new LocalCache(vocab), vocab);

			ConsoleReader reader = new ConsoleReader();
			reader.setPrompt("pinto> ");
			reader.addCompleter(new StringsCompleter(new PintoVocabulary().getCommands()));

			String line;
			PrintWriter out = new PrintWriter(reader.getOutput());

			while ((line = reader.readLine()) != null) {
				try {
					ArrayDeque<Data<?>> output = (ArrayDeque<Data<?>>) pinto.evaluateStatement(line);
					List<DoubleData> dd = new ArrayList<>();
					while(!output.isEmpty() && output.peekFirst() instanceof DoubleData) {
						dd.add((DoubleData) output.removeFirst());
					}
					if(dd.size() > 0) {
						PeriodicRange<?> range = dd.get(0).getRange();
						List<LocalDate> dates = range.dates();
						String[] labels = Stream.concat(Stream.of("Date"),dd.stream().map(Data::getLabel))
											.toArray(i -> new String[i]);
	 					String[][] table = new String[(int) range.size()][dd.size()+1];
						for(int i = 0; i < range.size(); i++) {
							table[i][0] = dates.get(i).toString();
						}
						for(AtomicInteger i = new AtomicInteger(0); i.get() < dd.size();i.incrementAndGet()) {
							AtomicInteger j = new AtomicInteger(0);
							dd.get(i.get()).getData().forEach(
									d -> table[j.getAndIncrement()][i.get()+1] = Double.toString(d));
						}
						out.println(FlipTable.of(labels, table));
						
					} else {
						output.stream().map(Object::toString).forEach(out::println);
					}
				} catch(PintoSyntaxException pse) {
					System.out.println("Pinto syntax problem: " + pse.getMessage());
					pse.printStackTrace();
				} catch (Exception e) {
					System.out.println("Evaluation error: " + e.getCause().getMessage());
					e.printStackTrace();
					
				}
				out.flush();
			}
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

}
