package tech.pinto.extras;

import static tech.pinto.Pinto.toTableConsumer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import tech.pinto.Cache;
import tech.pinto.Column;
import tech.pinto.Name;
import tech.pinto.PintoSyntaxException;
import tech.pinto.StandardVocabulary;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;

public class ExtraVocabulary extends StandardVocabulary {

	public BloombergClient bc = new BloombergClient();

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
		names.put("report", new Name("report", p -> t -> {
			LinkedList<Column<?,?>> s = t.peekStack();
			String title = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			String id = getId();
			StringBuilder sb = new StringBuilder();
			try (BufferedReader bf = 
				new BufferedReader(new FileReader(new File(
					getClass().getClassLoader().getResource("report_top.html").getFile())))) {
				bf.lines().forEach(l -> sb.append(l).append("\n"));
			} catch (IOException e) {
				throw new PintoSyntaxException("Unable to open report", e);
			}
			sb.append("<script type=\"text/javascript\">\n");
			sb.append("$('#mainTitle').text(\"").append(title);
			sb.append("\")\n</script>\n");
			while(!s.isEmpty()) {
				sb.append(((Column.OfConstantStrings) s.removeFirst()).getValue());
			}
			try {
				File f = File.createTempFile(id, ".html");
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
				throw new PintoSyntaxException("Unable to close report", e);
			}
    		t.setStatus("Report finished");
		}, "[title=\"Pinto report\",HTML]", "Creates a new HTML report containing all *HTML* columns", true));
		names.put("chart", new Name("chart", p -> toTableConsumer(s->  {
    		String startString = ((Column.OfConstantStrings)s.removeFirst()).getValue();
    		LocalDate start = startString.equals("today") ? LocalDate.now() : LocalDate.parse(startString);
    		String endString = ((Column.OfConstantStrings)s.removeFirst()).getValue();
    		LocalDate end = endString.equals("today") ? LocalDate.now() : LocalDate.parse(endString);
    		PeriodicRange<?> range = Periodicities.get(((Column.OfConstantStrings)s.removeFirst()).getValue())
    									.range(start, end, false);
    		String title = ((Column.OfConstantStrings)s.removeFirst()).getValue();
			String chartId = getId();
			HTMLBuilder h = new HTMLBuilder();
			h.l("<div id=\"" + chartId + "\"></div>");
			if(!title.equals("none")) {
				h.a("<h3>").a(title).l("</h3>");
			}
			h.l("<script type=\"text/javascript\">");
			h.l("var chart = c3.generate({");
			h.i().a("bindto: '#").a(chartId).l("',");
			h.i().l("data: {");
			h.i(2).l("x: 'x',");
			h.i(2).l("columns: [");
			h.i(3).l(range.dates().stream().map(LocalDate::toString)
					.collect(Collectors.joining("', '","['x', '","'],")));
			for(int i = s.size()-1; i >= 0; i--) {
				s.get(i).setRange(range);
				h.i(3).a("['").a(s.get(i).getHeader()).a("',");
				h.i(3).a(((Column.OfDoubles)s.get(i)).rows().mapToObj(String::valueOf).collect(Collectors.joining(", ","","]")));
				h.l(i > 0 ? "," : "");
			}
			h.i(3).l("]");
			h.i(2).l("},");
			h.i(2).l("axis: {");
			h.i(3).l("x: {");
			h.i(4).l("type: 'timeseries',");
			h.i(4).l("tick: {\r\n" + 
					"                format: '%Y-%m-%d'\r\n" + 
					"            }");
			h.i(3).l("},");
			h.i(3).l("y: {");
			h.i(4).l("format: d3.format(\".0%\")");
			h.i(3).l("}");
			h.i(2).l("},");
			h.i(2).l("point: {");
			h.i(3).l("show: false");
			h.i(2).l("}");
			h.l("});");
			h.l("</script>");	
			s.clear();
			s.add(new Column.OfConstantStrings(h.toString(), "HTML"));
		}),"[start=\"today\",end=\"today\",freq=\"B\",title=\"none\",:]", "Creates a const string column with code for an HTML chart.",false));
		/*names.put("fut", new Name(pinto -> t -> {
			final String[] monthCodes = new String[] { "H", "M", "U", "Z" };
			LinkedList<Column<?, ?>> s = t.peekStack();
			String contractCode = ((Column.OfConstantStrings) s.removeFirst()).getValue().toUpperCase();
			String contractYellowKey = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			String priceModifierFormula = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			String criteriaFieldCode = ((Column.OfConstantStrings) s.removeFirst()).getValue().trim().toUpperCase()
					.replaceAll("\\s+", "_");
			String priceFieldCode = ((Column.OfConstantStrings) s.removeFirst()).getValue().trim().toUpperCase()
					.replaceAll("\\s+", "_");
			String pricePreviousCode = ((Column.OfConstantStrings) s.removeFirst()).getValue().trim().toUpperCase()
					.replaceAll("\\s+", "_");
			int previousOffset = Integer.parseInt(((Column.OfConstantStrings) s.removeFirst()).getValue());
			boolean calcReturn = Boolean.parseBoolean(((Column.OfConstantStrings) s.removeFirst()).getValue());
			int numberOfContracts = 0;
			s.add(new Column.OfDoubles(inputs -> "fut(" + contractCode + ")", inputs -> range -> {
				// key is code ("Z-2010") and value is column index for data
				HashMap<String, Integer> contracts = new HashMap<>();
				// key is code and value is [start date, end date]
				HashMap<String, PeriodicRange<?>> contractStartEnd = new HashMap<>();
				// key is date and value is [current code, next code]
				List<String[]> contractsForDate = new ArrayList<>();
				// figure out current and next contract for each date
				for (Period p : range.values()) {
					LocalDate d = p.endDate();
					int monthIndex = (d.get(ChronoField.MONTH_OF_YEAR) - 1) / 3;
					int year = d.getYear();
					String[] codes = new String[] { monthCodes[monthIndex] + "-" + year,
							monthCodes[monthIndex == 3 ? 0 : monthIndex + 1] + "-"
									+ (year + (monthIndex == 3 ? 1 : 0)) }; // array of current code and next
					contractsForDate.add(codes);
					for (String code : codes) {
						if (!contracts.containsKey(code)) {
							contracts.put(code, numberOfContracts++);
							contractStartEnd.put(code, range.periodicity().range(p, p, range.clearCache()));
						} else {
							PeriodicRange<?> previousFirstLast = contractStartEnd.get(code);
							contractStartEnd.put(code,
									range.periodicity().range(
											previousFirstLast.start().isBefore(p) ? previousFirstLast.start() : p,
											previousFirstLast.end().isAfter(p) ? previousFirstLast.end() : p,
											range.clearCache()));
						}
					}
				}
				// download values for criteria field and prices (starting one day before for
				// prices)
				PeriodicRange<Period> expandedRange = (PeriodicRange<Period>) range.expand(-1);
				// final String formulaTemplate = "join(0,{3},bbg({0}
				// Comdty,{1},fillprevious),{2},0) {4}";
				final String formulaTemplate = "0 bbg({0} {5},{1}) [0] flb(W-FRI) 0 join({3},{2}) {4}";
				double[][] criteria = new double[contracts.size()][];
				double[][] prices = new double[contracts.size()][];
				double[][] pricesPrev = new double[contracts.size()][];
				for (Map.Entry<String, PeriodicRange<?>> e : contractStartEnd.entrySet()) {
					int contractRow = contracts.get(e.getKey());
					String[] monthYearCodes = e.getKey().split("-");
					String bbgContractCode = null;
					try {
						if (LocalDate.now().getYear() <= Integer.parseInt(monthYearCodes[1])) {
							bbgContractCode = contractCode + monthYearCodes[0] + monthYearCodes[1].substring(3, 4);
						} else {
							bbgContractCode = contractCode + monthYearCodes[0] + monthYearCodes[1].substring(2, 4);
						}
						criteria[contractRow] = runExpression(expandedRange,
								MessageFormat.format(formulaTemplate, bbgContractCode, criteriaFieldCode,
										e.getValue().end().next().endDate().toString(),
										e.getValue().start().endDate().toString(), "", contractYellowKey));
						prices[contractRow] = runExpression(expandedRange,
								MessageFormat.format(formulaTemplate, bbgContractCode, priceFieldCode,
										e.getValue().end().next().endDate().toString(),
										e.getValue().start().previous().endDate().toString(), priceModifierFormula,
										contractYellowKey));
						if (pricePreviousCode.equals(priceFieldCode)) {
							pricesPrev[contractRow] = prices[contractRow];
						} else {
							pricesPrev[contractRow] = runExpression(expandedRange,
									MessageFormat.format(formulaTemplate, bbgContractCode, pricePreviousCode,
											e.getValue().end().next().endDate().toString(),
											e.getValue().start().previous().endDate().toString(), priceModifierFormula,
											contractYellowKey));
						}
					} catch (IllegalArgumentException badFormula) { // recent contract needs only one digit year
						throw badFormula;
					} catch (Exception wrongCode) { // recent contract needs only one digit year
						wrongCode.printStackTrace();
						bbgContractCode = contractCode + monthYearCodes[0] + monthYearCodes[1].substring(3, 4);
						try {
							criteria[contractRow] = runExpression(expandedRange,
									MessageFormat.format(formulaTemplate, bbgContractCode, criteriaFieldCode,
											e.getValue().end().next().endDate().toString(),
											e.getValue().start().endDate().toString(), ""));
						} catch (Exception notfound) {
						}
						try {
							prices[contractRow] = runExpression(expandedRange,
									MessageFormat.format(formulaTemplate, bbgContractCode, priceFieldCode,
											e.getValue().end().next().endDate().toString(),
											e.getValue().start().previous().endDate().toString(), priceModifierFormula,
											contractYellowKey));
						} catch (Exception notfound) {
						}
					}
				}
				Builder d = DoubleStream.builder();
				for (int i = 1; i < expandedRange.size(); i++) {
					int row1 = contracts.get(contractsForDate.get(i - 1)[0]);
					int row2 = contracts.get(contractsForDate.get(i - 1)[1]);
					int currentRow = row1;
					if (isNG(prices[row1][i]) || isNG(prices[row1][i + previousOffset])) {
						currentRow = row2;
					} else if (isNG(prices[row2][i]) || isNG(prices[row2][i + previousOffset])) {
						currentRow = row1;
					} else if (isNG(pricesPrev[row2][i]) || isNG(pricesPrev[row2][i + previousOffset])) {
						currentRow = row1;
					} else if (isNG(pricesPrev[row1][i]) || isNG(pricesPrev[row1][i + previousOffset])) {
						currentRow = row2;
					} else if (isNG(criteria[row1][i]) && isNG(criteria[row2][i])) {
						currentRow = row1; // default to c1 if criteria are bad for both
					} else if (Double.isNaN(criteria[row1][i])) {
						currentRow = row2;
					} else if (Double.isNaN(criteria[row2][i])) {
						currentRow = row1;
					} else {
						currentRow = criteria[row1][i] >= criteria[row2][i] ? row1 : row2;
					}
					if (calcReturn) {
						d.accept(prices[currentRow][i] / pricesPrev[currentRow][i + previousOffset] - 1.0d);
					} else {
						d.accept(prices[currentRow][i]);
					}
				}
				return d.build();

			}));

		}, Optional.empty(), Optional.of(
				"[code,yellow_key=\"Comdty\",price_modifier=\"\",criteria_field=\"OPEN_INT\",price_field=\"PX_LAST\","
						+ "price_field_previous=\"PX_LAST\",previous_offset=-1,calc_return=\"true\"]"),
				"Calculates returns for front contract of given futures contract series", false, false));*/
	}

//    private boolean isNG(double d) {
//        return Double.isNaN(d) || d == 0.0d || Double.isInfinite(d);
//    }
	private static String getId() {
		return UUID.randomUUID().toString().replaceAll("\\d", "").replaceAll("-", "");
	}
	
	private static class HTMLBuilder {
		private final StringBuilder sb = new StringBuilder();
		
		public HTMLBuilder l(String s) {
			sb.append(s).append("\n");
			return this;
		}

		public HTMLBuilder a(String s) {
			sb.append(s);
			return this;
		}
		
		public HTMLBuilder i() {
			return i(1);
		}

		public HTMLBuilder i(int n) {
			for(int i = 0; i < n; i++) {
				sb.append("\t");
			}
			return this;
		}
		
		public String toString() {
			return sb.toString();
		}
		
		
	}
}
