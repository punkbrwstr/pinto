package tech.pinto;

import java.io.IOException;


import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import jline.console.ConsoleReader;
import tech.pinto.tools.LogAppender;

public class Console implements Runnable {
	
	private final Pinto pinto;
	private final int port;
	private final String build;
	private final Runnable[] shutdownCommands;
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	private boolean trace = false;

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
			nf.setMinimumFractionDigits(4);
			nf.setMaximumFractionDigits(8);
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
					} else if(line.startsWith("\\digits")) {
						try {
							int digits = Integer.parseInt(line.split(" ")[1]);
							nf.setMaximumFractionDigits(digits);
							nf.setMinimumFractionDigits(digits);
						} catch(Throwable t) {
							out.println("Usage: \"\\digits #\" set fraction digits");
						}
					} else if(line.startsWith("\\trace")) {
						trace = ! trace;
					} else {
						out.println("Console options:");
						out.println("\t\"\\log\" print message log");
						out.println("\t\"\\nf default|percent|currency\" set output number format");
						out.println("\t\"\\digits #\" set fraction digits");
						out.println("\t\"\\df <format string>\" set output date format");
						out.println("\t\"\\trace\" toggle trace on/off");
					}
				} else {
					try {
						List<Table> l = pinto.evaluate(line.toString()); 
						if(l.size() == 0) {
				    		out.println("");
						} else {
							for(Table t : l) {
								long start = System.nanoTime();
								out.println(t.getStatus().isPresent() ? t.getStatus().get() : t.getConsoleText(nf, trace));
								log.info("console elapsed: {}ms", (System.nanoTime() - start) / 1000000d);
							}
						}
					} catch (PintoSyntaxException pse) {
						StringBuilder sb = new StringBuilder();
						sb.append("Syntax error: ");
						String[] s = pse.getLocalizedMessage().split(":");
						for(int i = 0; i < s.length; i++) {
							sb.append(i != 0 ? "\tcaused by: " : "").append(s[i]).append(System.lineSeparator());
						}
						System.out.print(sb.toString());
						//System.out.println("Incorrect syntax: " + pse.getLocalizedMessage());
						pse.printStackTrace();
					} catch (Throwable e) {
						System.out.println("Evaluation error: " + e.getMessage());
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
