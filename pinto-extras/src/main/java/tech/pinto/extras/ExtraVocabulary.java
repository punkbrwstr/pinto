package tech.pinto.extras;

import static tech.pinto.Pinto.toTableConsumer;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tech.pinto.Cache;
import tech.pinto.Column;
import tech.pinto.Name;
import tech.pinto.Pinto;
import tech.pinto.PintoSyntaxException;
import tech.pinto.StandardVocabulary;
import tech.pinto.Table;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.DoubleCollectors;

public class ExtraVocabulary extends StandardVocabulary {

	public BloombergMarketData bc = new BloombergMarketData();
    private Optional<MessageFormat> chartHTML = Optional.empty();

	public ExtraVocabulary() {
		names.put("bbg", new Name("bbg", toTableConsumer(s -> {
			String tickersString = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			List<String> tickers = Arrays.asList(tickersString.split(","));
			String fieldsString = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			List<String> fields = Arrays.stream(fieldsString.split(",")).map(f -> f.trim()).map(String::toUpperCase)
					.map(f -> f.replaceAll(" ", "_")).collect(Collectors.toList());
			String key = tickersString + fieldsString;
			List<String> tickersfields = tickers.stream().flatMap(t -> fields.stream().map(c -> t + ":" + c))
					.collect(Collectors.toList());
			for (int i = 0; i < tickersfields.size(); i++) {
				final int index = i;
				s.addFirst(new Column.OfDoubles(inputs -> tickersfields.get(index), inputs -> range -> {
					return Cache.getCachedValues(key, range, index, tickersfields.size(),
							bc.getFunction(tickers, fields));
				}));
			}

		}), "[tickers,fields=\"PX_LAST\"]", "Downloads Bloomberg history for each *fields* for each *tickers*"));
    	names.put("report", new Name("rpt", p -> t -> {
    		Pinto.State state = p.getState();
			String id = getId();
			p.getNamespace().define("_rpt-" + id , state.getNameIndexer(), "Report id: " + id,
					state.getDependencies().subList(0,state.getDependencies().size() - 1), state.getPrevious());
			try {
				int port = p.getPort();
				Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + port + "/pinto/report?p=" + id));
			} catch (Exception e) {
				throw new PintoSyntaxException("Unable to open report", e);
			}
    		t.setStatus("Report id: " + id);
    	}, Optional.empty(), Optional.of("[HTML]"), "Creates an HTML page from any columns labelled HTML.", true, true, true));
		names.put("grid", new Name("grid", p -> toTableConsumer(s->  {
    		int columns = Double.valueOf(((Column.OfConstantDoubles)s.removeFirst()).getValue()).intValue();
			StringBuilder sb = new StringBuilder();
			sb.append("\n<table class=\"pintoGrid\">\n\t<tbody>\n");
			for(int i = 0; i < s.size(); ) {
				if(i % columns == 0) {
					sb.append("\t\t<tr>\n");
				}
				sb.append("\t\t\t<td>").append(((Column.OfConstantStrings)s.get(s.size() - i++ - 1)).getValue()).append("</td>\n");
				if(i % columns == 0 || i == s.size()) {
					sb.append("\t\t</tr>\n");
				}
			}
			sb.append("\t</tbody>\n</table>\n");
			s.clear();
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}),"[columns=3,HTML]", "Creates a grid layout in a report with all input columns labelled HTML as cells in the grid.",false));
		names.put("table", new Name("table", p -> toTableConsumer(s->  {
    		Periodicity<?> periodicity = ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
    		LinkedList<LocalDate> d = new LinkedList<>();
    		while((!s.isEmpty()) && d.size() < 2 && s.peekFirst().getHeader().equals("date")) {
    			d.add(((Column.OfConstantDates)s.removeFirst()).getValue());
    		}
    		PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek(), false);
    		NumberFormat nf = ((Column.OfConstantStrings)s.removeFirst()).getValue().equals("percent") ? NumberFormat.getPercentInstance() :
    								NumberFormat.getNumberInstance();
    		nf.setGroupingUsed(false);
    		s.stream().forEach(c -> c.setRange(range));
    		Table t = new Table();
    		t.insertAtTop(s);
    		String[] lines = t.toCsv(nf).split("\n");
    		s.clear();
    		StringBuilder sb = new StringBuilder();
    		sb.append("<table class=\"pintoTable\">\n<thead>\n");
    		Arrays.stream(lines[0].split(",")).forEach(h -> sb.append("\t<th class=\"rankingTableHeader\">").append(h).append("</th>\n"));
    		sb.append("</thead>\n<tbody>\n");
    		Arrays.stream(lines).skip(1).map(l -> l.split(",")).forEach(l -> {
    			sb.append("<tr>\n");
    			Arrays.stream(l).forEach(c -> sb.append("<td>").append(c).append("</td>"));
    			sb.append("</tr>\n");
    		});
   			sb.append("</tbody></table>\n");
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}),"[periodicity=B, date=[count=-20] offset today,format=\"decimal\",:]", "Creates a const string column with code for an HTML ranking table.",false));
		names.put("chart", new Name("chart", p -> toTableConsumer(s->  {
    		Periodicity<?> periodicity = ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
    		LinkedList<LocalDate> d = new LinkedList<>();
    		while((!s.isEmpty()) && d.size() < 2 && s.peekFirst().getHeader().equals("date")) {
    			d.add(((Column.OfConstantDates)s.removeFirst()).getValue());
    		}
    		PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek(), false);
    		String title = ((Column.OfConstantStrings)s.removeFirst()).getValue();
			if(!chartHTML.isPresent()) {
				try {
					chartHTML = Optional.of(new MessageFormat(readInputStreamIntoString((getClass()
							.getClassLoader().getResourceAsStream("report_chart.html")))));
				} catch (IOException e) {
					throw new PintoSyntaxException("Unable to open chart html template", e);
				}
			}
			String dates = range.dates().stream().map(LocalDate::toString).collect(Collectors.joining("', '","['x', '","']"));
			StringBuilder data = new StringBuilder();
			for(int i = s.size()-1; i >= 0; i--) {
				s.get(i).setRange(range);
				data.append("['").append(s.get(i).getHeader()).append("',");
				data.append(((Column.OfDoubles)s.get(i)).rows().mapToObj(String::valueOf).collect(Collectors.joining(", ","","]")));
				data.append(i > 0 ? "," : "");
			}
			String html = chartHTML.get().format(new Object[] {getId(), dates, data, title}, new StringBuffer(), null).toString();
			s.clear();
			s.add(new Column.OfConstantStrings(html, "HTML"));
		}),"[periodicity=B, date=[count=-20] offset today,title=\"\",:]", "Creates a const string column with code for an HTML chart.",false));
		names.put("rt", new Name("rt", p -> toTableConsumer(s->  {
			LinkedList<LocalDate> starts = new LinkedList<>();
			while((!s.isEmpty()) && s.peekFirst().getHeader().equals("starts")) {
				starts.addFirst(((Column.OfConstantDates)s.removeFirst()).getValue());
			}
			LinkedList<LocalDate> ends = new LinkedList<>();
			while((!s.isEmpty()) && s.peekFirst().getHeader().equals("ends")) {
				ends.addFirst(((Column.OfConstantDates)s.removeFirst()).getValue());
			}
			LinkedList<String> columnLabels = new LinkedList<>();
			while((!s.isEmpty()) && s.peekFirst().getHeader().equals("labels")) {
				columnLabels.addFirst(((Column.OfConstantStrings)s.removeFirst()).getValue());
			}
			LinkedList<Periodicity<?>> periodicities = new LinkedList<>();
			while((!s.isEmpty()) && s.peekFirst().getHeader().equals("periodicities")) {
				periodicities.addFirst(((Column.OfConstantPeriodicities)s.removeFirst()).getValue());
			}
			LinkedList<DoubleCollectors> collectors = new LinkedList<>();
			while((!s.isEmpty()) && s.peekFirst().getHeader().equals("collectors")) {
				collectors.addFirst(DoubleCollectors.valueOf(((Column.OfConstantStrings)s.removeFirst()).getValue()));
			}
    		DoubleCollectors[] dc = Arrays.stream(((Column.OfConstantStrings)s.removeFirst()).getValue().split(","))
    									.map(String::trim).map(str -> DoubleCollectors.valueOf(str)).toArray(DoubleCollectors[]::new);
    		NumberFormat nf = ((Column.OfConstantStrings)s.removeFirst()).getValue().equals("percent") ? NumberFormat.getPercentInstance() :
    								NumberFormat.getNumberInstance();
    		int digits = ((Column.OfConstantDoubles)s.removeFirst()).getValue().intValue();
    		nf.setMaximumFractionDigits(digits);
    		int columns = starts.size();
    		int rows = s.size();
    		String[] labels = new String[rows];
    		String[] headers = new String[columns];
    		String[][] cells = new String[rows][columns];
    		for(int i = 0; i < columns; i++) {
    			LocalDate start = starts.get(Math.min(i, columns - 1));
    			LocalDate end = ends.get(Math.min(i, columns - 1));
    			PeriodicRange<?> pr = periodicities.get(Math.min(i, periodicities.size() - 1)).range(start, end, false);
    			String columnLabel = i >= columnLabels.size() ? "" : columnLabels.get(i);
    			headers[i] = columnLabel.equals("") ? start + " - " + end : columnLabel;
    			List<Column<?,?>> l = null;
    			if(i == columns - 1) {
    				l = new ArrayList<>(s); 
    				s.clear();
    			} else {
    				l = new ArrayList<>();
                    s.stream().map(Column::clone).forEach(l::add);
    			}
    			double[][] values = new double[l.size()][2];
    			for(int j = 0; j < l.size(); j++) {
    				l.get(j).setRange(pr);
    				if(i == 0) {
    					labels[j] = l.get(j).getHeader();
    				}
    				values[j][0] =  ((Column.OfDoubles) l.get(j)).rows().collect(dc[Math.min(i, dc.length - 1)], (v,d) -> v.add(d), (v,v1) -> v.combine(v1)).finish();
    				values[j][1] = (int) j;
    			}
    			Arrays.sort(values, (c1, c2) ->  c1[0] == c2[0] ? 0 : c1[0] < c2[0] ? 1 : -1);
    			for(int j = 0; j < values.length; j++) {
    				StringBuilder sb = new StringBuilder();
    				sb.append("\t<td id=\"rankingColor").append((int)values[j][1])
    					.append("\" class=\"rankingTableCell\">").append(labels[(int)values[j][1]]).append(": ").append(nf.format(values[j][0])).append("</td>\n");
    				cells[j][i] = sb.toString();
    			}
    		}
    		StringBuilder sb = new StringBuilder();
    		sb.append("<table class=\"rankingTable\">\n<thead>\n");
    		Arrays.stream(headers).forEach(h -> sb.append("\t<th class=\"rankingTableHeader\">").append(h).append("</th>\n"));
    		sb.append("</thead>\n<tbody>\n");
    		for(int i = 0; i < cells.length; i++) {
    			sb.append("<tr>\n");
    			for(int j = 0; j < columns; j++) {
    				sb.append(cells[i][j]);
    			}
    			sb.append("</tr>\n");
    		}
   			sb.append("</tbody></table>\n");
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}),"[starts=[periodicity=BA-DEC] offset,ends=today,labels=\"YTD\",periodicities=B,functions=\"pct_change\",format=\"percent\",digits=2,:]", "Creates a const string column with code for an HTML ranking table.",false));
	}
	
	private static String getId() {
		String id = null;
		do {
			id = UUID.randomUUID().toString().replaceAll("\\d", "").replaceAll("-", "");
		} while(id.length() < 8);
		return id.substring(0,8);
	}

	private String readInputStreamIntoString(InputStream inputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while(result != -1) {
		    buf.write((byte) result);
		    result = bis.read();
		}
		return buf.toString("UTF-8");
	}

}
