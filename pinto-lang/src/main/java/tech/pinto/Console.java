package tech.pinto;

import java.io.IOException;


import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.jakewharton.fliptables.FlipTable;

import jline.console.ConsoleReader;
import tech.pinto.function.TerminalFunction;
import tech.pinto.tools.LogAppender;
import tech.pinto.tools.Outputs;

public class Console implements Runnable {
	
	private final Pinto pinto;
	private final int port;
	private final String build;
	private final Runnable[] shutdownCommands;

	public Console(Pinto pinto, int port, String build, Runnable...shutdownCommands) {
		this.pinto = pinto;
		this.port = port;
		this.build = build;
		this.shutdownCommands = shutdownCommands;
	}

	@Override
	public void run() {
		try {
			NumberFormat nf = NumberFormat.getInstance();
			ConsoleReader reader = new ConsoleReader();
			reader.setPrompt("pinto> ");
			reader.addCompleter(pinto.getNamespace());
			String line;
			PrintWriter out = new PrintWriter(reader.getOutput());
			out.println("Pinto (build: " + build + ")");
			out.println("Server started on http://127.0.0.1:" + port);
			out.println("For pinto help type \"help\".  To quit type \"\\q\". To show other console options type \"\\help\".");

			while ((line = reader.readLine()) != null) {
				if (line.indexOf("\\") == 0) {
					if(line.startsWith("\\q")) {
						Arrays.stream(shutdownCommands).forEach(r -> r.run());
						return;
					} else if(line.startsWith("\\log")) {
						while(!LogAppender.LOG.isEmpty()) {
							try {
								System.out.println(LogAppender.LOG.take());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else if(line.startsWith("\\nf")) {
						String[] nfs = line.split(" ");
						if(nfs.length > 1 && nfs[1].equals("default")) {
							nf = NumberFormat.getInstance();
						} else if(nfs.length > 1 && nfs[1].equals("percent")) {
							nf = NumberFormat.getPercentInstance();
							nf.setMaximumFractionDigits(2);
							nf.setMinimumFractionDigits(2);
						} else if(nfs.length > 1 && nfs[1].equals("currency")) {
							nf = NumberFormat.getCurrencyInstance();
						}


					} else if(line.startsWith("\\help")) {
						out.println("Other console options:");
						out.println("\t\"\\log\" print message log");
						out.println("\t\"\\nf default|percent|currency\" set output number format");
						out.println("\t\"\\df <format string>\" set output date format");
					}
				} else {
					try {
				    	for(TerminalFunction tf : pinto.execute(line.toString())) {
				    		Optional<LinkedList<TimeSeries>> list = tf.getTimeSeries();
				    		if(list.isPresent()) {
				    			streamInReverse(list.get()).collect(Outputs.doubleDataToStringTable(nf))
				    				.ifPresent(table -> out.println(FlipTable.of(table.getHeader(), table.getCells())));
				    		}
				    		tf.getText().ifPresent(out::println);
				    	}
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
	
	private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		  Iterator<T> descendingIterator = input.descendingIterator();
		  return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
		    descendingIterator, Spliterator.ORDERED), false);
	}

}
