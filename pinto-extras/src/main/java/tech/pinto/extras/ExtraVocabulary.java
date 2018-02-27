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
import java.util.Arrays;
import java.util.ArrayList;
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
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.DoubleCollectors;

public class ExtraVocabulary extends StandardVocabulary {

	public BloombergClient bc = new BloombergClient();
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
					return Cache.getCachedValues(key, range, bc.getFunction(tickers, fields)).get(index);
				}));
			}

		}), "[tickers,fields=\"PX_LAST\"]", "Downloads Bloomberg history for each *fields* for each *tickers*"));
    	names.put("rpt", new Name("rpt", p -> t -> {
    		Pinto.State state = p.getState();
			String id = getId();
			p.getNamespace().define("_rpt-" + id , state.getNameIndexer(), "Report id: " + id,
					state.getDependencies().subList(0,state.getDependencies().size() - 1), state.getPrevious());
			try {
				Desktop.getDesktop().browse(new URI("http://127.0.0.1:5556/pinto/report?p=" + id));
			} catch (Exception e) {
				throw new PintoSyntaxException("Unable to open report", e);
			}
    		t.setStatus("Report id: " + id);
    	}, Optional.empty(), Optional.of("[HTML]"), "Creates an HTML page from any columns labelled HTML.", true, true, true));
		names.put("chart", new Name("chart", p -> toTableConsumer(s->  {
    		String startString = ((Column.OfConstantStrings)s.removeFirst()).getValue();
    		LocalDate start = startString.equals("today") ? LocalDate.now() : LocalDate.parse(startString);
    		String endString = ((Column.OfConstantStrings)s.removeFirst()).getValue();
    		LocalDate end = endString.equals("today") ? LocalDate.now() : LocalDate.parse(endString);
    		PeriodicRange<?> range = Periodicities.get(((Column.OfConstantStrings)s.removeFirst()).getValue())
    									.range(start, end, false);
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
		}),"[start=\"today\",end=\"today\",freq=\"B\",title=\"\",:]", "Creates a const string column with code for an HTML chart.",false));
		names.put("rt", new Name("rt", p -> toTableConsumer(s->  {
    		int[] startOffsets = Arrays.stream(((Column.OfConstantStrings)s.removeFirst()).getValue().split(","))
    									.mapToInt(Integer::parseInt).toArray();
    		int[] endOffsets = Arrays.stream(((Column.OfConstantStrings)s.removeFirst()).getValue().split(","))
    									.mapToInt(Integer::parseInt).toArray();
    		Periodicity<?>[] startFreqs = Arrays.stream(((Column.OfConstantStrings)s.removeFirst()).getValue().split(","))
    												.map(str -> Periodicities.get(str)).toArray(Periodicity[]::new);
    		Periodicity<?>[] endFreqs = Arrays.stream(((Column.OfConstantStrings)s.removeFirst()).getValue().split(","))
    												.map(str -> Periodicities.get(str)).toArray(Periodicity[]::new);
    		DoubleCollectors[] dc = Arrays.stream(((Column.OfConstantStrings)s.removeFirst()).getValue().split(","))
    									.map(str -> DoubleCollectors.valueOf(str)).toArray(DoubleCollectors[]::new);
    		Periodicity<?>[] freqs = Arrays.stream(((Column.OfConstantStrings)s.removeFirst()).getValue().split(","))
    												.map(str -> Periodicities.get(str)).toArray(Periodicity[]::new);
    		NumberFormat nf = ((Column.OfConstantStrings)s.removeFirst()).getValue().equals("percent") ? NumberFormat.getPercentInstance() :
    								NumberFormat.getNumberInstance();
    		int digits = ((Column.OfConstantDoubles)s.removeFirst()).getValue().intValue();
    		nf.setMaximumFractionDigits(digits);
    		if(startOffsets.length != endOffsets.length /*|| startOffsets.length != startFreqs.length || startOffsets.length != endFreqs.length */) {
    			throw new PintoSyntaxException("Offset lists must have same length.");
    		}
    		String[] labels = new String[s.size()];
    		String[] headers = new String[startOffsets.length];
    		String[][] cells = new String[s.size()][startOffsets.length];
    		for(int i = 0; i < startOffsets.length; i++) {
    			LocalDate start = startFreqs[Math.min(i, startFreqs.length - 1)].from(LocalDate.now()).offset(-1 * Math.abs(startOffsets[i])).endDate();
    			LocalDate end = endFreqs[Math.min(i, endFreqs.length - 1)].from(LocalDate.now()).offset(-1 * Math.abs(endOffsets[i])).endDate();
    			PeriodicRange<?> pr = freqs[Math.min(i, freqs.length - 1)].range(start, end, false);
    			headers[i] = start + " - " + end;
    			List<Column<?,?>> l = null;
    			if(i == startOffsets.length - 1) {
    				l = s; 
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
    		sb.append("</thead>\n</tbody>\n");
    		for(int i = 0; i < cells.length; i++) {
    			sb.append("<tr>\n");
    			for(int j = 0; j < startOffsets.length; j++) {
    				sb.append(cells[i][j]);
    			}
    			sb.append("</tr>\n");
    		}
   			sb.append("</tbody></table>\n");
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}),"[start_offsets,end_offsets,start_freqs,end_freqs,functions=\"changepct\",freqs=\"B\",format=\"percent\",digits=2,:]", "Creates a const string column with code for an HTML ranking table.",false));
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
