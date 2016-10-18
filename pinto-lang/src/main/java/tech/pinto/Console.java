package tech.pinto;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jakewharton.fliptables.FlipTable;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import tech.pinto.tools.LogAppender;
import tech.pinto.tools.Outputs;

public class Console implements Runnable {
	
	private final Pinto pinto;
	private final int port;
	private final String build;

	public Console(Pinto pinto, int port, String build) {
		this.pinto = pinto;
		this.port = port;
		this.build = build;
	}

	@Override
	public void run() {
		try {
			ConsoleReader reader = new ConsoleReader();
			reader.setPrompt("pinto> ");
			reader.addCompleter(new StringsCompleter(pinto.getVocab().getCommandNames()));
			String line;
			PrintWriter out = new PrintWriter(reader.getOutput());
			out.println("Pinto (build: " + build + ")");
			out.println("Server started on http://127.0.0.1:" + port);
			out.println("For help type \"help\".  To quit type \"\\q\". To show log type \"\\log\".");

			while ((line = reader.readLine()) != null) {
				if (line.indexOf("\\") == 0) {
					if(line.startsWith("\\q")) {
						break;
					} else if(line.startsWith("\\log")) {
						while(!LogAppender.LOG.isEmpty()) {
							try {
								System.out.println(LogAppender.LOG.take());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					try {
						List<Pinto.Response> output = pinto.execute(line.toString());
						List<TimeSeries> data = output.stream().map(Pinto.Response::getTimeseriesOutput)
								.filter(Optional::isPresent).map(Optional::get).flatMap(List::stream)
								.collect(Collectors.toList());
						if (data.size() > 0) {
							Optional<Outputs.StringTable> t = data.stream()
									.collect(Outputs.doubleDataToStringTable());
							if (t.isPresent()) {
								out.println(FlipTable.of(t.get().getHeader(), t.get().getCells()));
							}
						}
						output.stream().map(Pinto.Response::getMessageOutput).filter(Optional::isPresent)
								.map(Optional::get).forEach(out::println);
					} catch (PintoSyntaxException pse) {
						System.out.println("Incorrect syntax: " + pse.getMessage());
						pse.printStackTrace();
					} catch (Throwable e) {
						System.out.println("Evaluation error");
						e.printStackTrace();
					}
				}
				out.flush();
			}
		} catch (IOException e) {
			System.out.println(e);

		}
	}

}
