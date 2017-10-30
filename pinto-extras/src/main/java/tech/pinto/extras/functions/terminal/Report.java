package tech.pinto.extras.functions.terminal;


import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tech.pinto.Column;
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Parameters;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.TerminalFunction;
import tech.pinto.time.Periodicities;

public class Report {
	
	private static final HashMap<String,StringBuilder> reports = new HashMap<>();
	
	private static final Parameters.Builder OPENER_PARAMETERS_BUILDER = new Parameters.Builder()
			.add("name", true, "Name to refer to report.")
			.add("title", false, "Title for report.")
			;
	public static final FunctionHelp.Builder OPENER_HELP_BUILDER = new FunctionHelp.Builder()
			.description("Starts an HTML report.")
			.parameters(OPENER_PARAMETERS_BUILDER.build());
	public static TerminalFunction getOpener(String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer) {
		return new TerminalFunction(name,namespace,previousFunction,indexer) {
			{
				parameters = Optional.of(OPENER_PARAMETERS_BUILDER.build());
			}

			@Override
			public Table getTable() throws PintoSyntaxException {
				compose();
				String name = parameters.get().getArgument("name");
				StringBuilder sb = new StringBuilder();
				try (BufferedReader bf = 
					new BufferedReader(new FileReader(new File(
						getClass().getClassLoader().getResource("report_top.html").getFile())))) {
					bf.lines().forEach(l -> sb.append(l).append("\n"));
				} catch (IOException e) {
					throw new PintoSyntaxException("Unable to open report " + name, e);
				}
				if(parameters.get().hasArgument("title")) {
					sb.append("<script type=\"text/javascript\">\n");
					sb.append("$('#mainTitle').text(\"");
					sb.append(parameters.get().getArgument("title"));
					sb.append("\")\n</script>\n");
				}
				reports.put(name, sb);
				return createTextColumn("Report " + name + " started.");
			}};
	}

	private static final Parameters.Builder CHART_PARAMETERS_BUILDER = new Parameters.Builder()
			.add("name", true, "Report on which to print the chart.")
			.add("start", LocalDate.now().toString(), "Start date of range to evaluate (format: yyyy-mm-dd)")
			.add("end", LocalDate.now().toString(), "End date of range to evaluate (format: yyyy-mm-dd)")
			.add("freq", "B", "Periodicity of range to evaluate " +
						Periodicities.allCodes().stream().collect(Collectors.joining(",", "{", "}")));
			;
	public static final FunctionHelp.Builder CHART_HELP_BUILDER = new FunctionHelp.Builder()
			.description("Starts an HTML report.")
			.parameters(CHART_PARAMETERS_BUILDER.build());
	public static TerminalFunction getChart(String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer) {
		return new TerminalFunction(name,namespace,previousFunction,indexer) {
			{
				parameters = Optional.of(CHART_PARAMETERS_BUILDER.build());
			}

			@Override
			public Table getTable() throws PintoSyntaxException {
				compose();
				String name = parameters.get().getArgument("name");
				String chartId = UUID.randomUUID().toString();
				if(!reports.containsKey(name)) {
					throw new PintoSyntaxException("No report started named " + name + ".");
				}

				StringBuilder sb = reports.get(name);
				Table t = new Table(compose(), Optional.of(Periodicities.get(parameters.get().getArgument("freq"))
						.range(LocalDate.parse(parameters.get().getArgument("start")),
								LocalDate.parse(parameters.get().getArgument("end")), false)));
				sb.append("<div id=\"" + chartId + "\"></div>");
				sb.append("<script type=\"text/javascript\">\n");
				sb.append("var chart = c3.generate({\n");
				sb.append("bindto: '#").append(chartId).append("',\n");
				sb.append("data: {\n\tx: 'x',\n\tcolumns: [");
				sb.append(t.getRange().get().dates().stream().map(LocalDate::toString)
						.collect(Collectors.joining("', '","\t\t['x', '","'],\n")));
				for(int i = 0; i < t.getColumnCount(); i++) {
					sb.append("\t\t['").append(t.getHeaders().get(i)).append("', ");
					sb.append(t.getSeries(i).mapToObj(String::valueOf).collect(Collectors.joining("', '","'","']")));
					sb.append(i != t.getColumnCount() -1 ? ",\n" : "\n");
				}
				
				sb.append("\t]\n},");
				sb.append("axis: {\n\tx: {\n\t\ttype: 'timeseries'\n\t}\n}\n});");
				sb.append("</script>\n");
				return createTextColumn("Added chart to " + name + ".");
			}};
	}

	private static final Parameters.Builder CLOSER_PARAMETERS_BUILDER = new Parameters.Builder()
			.add("name", true, "Name to refer to report.")
			;
	public static final FunctionHelp.Builder CLOSER_HELP_BUILDER = new FunctionHelp.Builder()
			.description("Finishes and opens an HTML report.")
			.parameters(CLOSER_PARAMETERS_BUILDER.build());
	public static TerminalFunction getCloser(String name, Namespace namespace,
			ComposableFunction previousFunction, Indexer indexer) {
		return new TerminalFunction(name,namespace,previousFunction,indexer) {
			{
				parameters = Optional.of(CLOSER_PARAMETERS_BUILDER.build());
			}

			@Override
			public Table getTable() throws PintoSyntaxException {
				compose();
				String name = parameters.get().getArgument("name");
				StringBuilder sb = reports.remove(name);
				try {
					File f = File.createTempFile(name, ".html");
					try(BufferedReader bf = 
							new BufferedReader(new FileReader(new File(
								getClass().getClassLoader().getResource("report_bottom.html").getFile())));
						PrintWriter pw = new PrintWriter(new FileWriter(f));
						) {
						bf.lines().forEach(l -> sb.append(l).append("\n"));
						pw.print(sb.toString());
					}
					Desktop.getDesktop().browse(f.toURI());
				} catch (IOException e) {
					throw new PintoSyntaxException("Unable to close report " + name, e);
				}
				return createTextColumn("Report " + name + " finished.");
			}};
	}

}
