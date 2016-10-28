package tech.pinto;

import java.io.IOException;


import java.io.PrintWriter;
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
			reader.addCompleter(pinto.getNamespace());
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
				    	for(TerminalFunction tf : pinto.execute(line.toString())) {
				    		Optional<LinkedList<TimeSeries>> list = tf.getTimeSeries();
				    		if(list.isPresent()) {
				    			streamInReverse(list.get()).collect(Outputs.doubleDataToStringTable())
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
