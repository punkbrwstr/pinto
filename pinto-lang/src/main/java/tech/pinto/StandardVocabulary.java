package tech.pinto;

import static tech.pinto.Name.nameBuilder;
import static tech.pinto.Column.*;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import tech.pinto.Column.ConstantColumn;
import tech.pinto.Column.RowsFunction;
import tech.pinto.Column.RowsFunctionGeneric;
import tech.pinto.Pinto.StackFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.DoubleCollector;

public class StandardVocabulary extends Vocabulary {
    
    protected final List<Name> names;
	private static Optional<MessageFormat> chartHTML = Optional.empty();
	
	public StandardVocabulary() {
		names = Arrays.asList(
			nameBuilder("def", StandardVocabulary::def)
				.description("Defines the expression as the preceding name literal.")
				.indexer(Indexer.NONE)
				.terminal()
				.startFromLast(),
			nameBuilder("undef", StandardVocabulary::undef)
				.description("Defines the expression as the preceding name literal.")
				.indexer(Indexer.NONE)
				.terminal(),
			nameBuilder("help", StandardVocabulary::help)
				.description("Prints help for the preceding name literal or all names if one has not been specified.")
				.indexer(Indexer.NONE)
				.terminal(),
			nameBuilder("list", StandardVocabulary::list)
				.description("Shows description for all names.")
				.indexer(Indexer.NONE)
				.terminal(),
			nameBuilder("eval", StandardVocabulary::eval)
				.description("Evaluates the expression over date range between two *date* columns over *periodicity*.")
				.indexer("[periodicity=B,date=today today,:]")
				.terminal(),
			nameBuilder("import", StandardVocabulary::imp)
				.description("Executes pinto expressions contained files specified by file names in string columns.")
				.terminal(),
			nameBuilder("to_csv", StandardVocabulary::to_csv)
				.indexer("[filename,periodicity=B,date=today today,:]")
				.description("Evaluates the expression over the date range specified by *start, *end* and *freq* columns, exporting the resulting table to csv *filename*.")
				.terminal(),


	/* stack manipulation */
			nameBuilder("only", StandardVocabulary::only)
				.description("Clears stack except for indexed columns."),
			nameBuilder("clear", StandardVocabulary::clear)
				.description("Clears indexed columns from table."),
			nameBuilder("rev", StandardVocabulary::rev)
				.description("Reverses order of input columns."),
			nameBuilder("pull", StandardVocabulary::pull)
				.description("Brings input columns to the front."),
			nameBuilder("copy", StandardVocabulary::copy)
				.indexer("[c=2,:]")
				.description("Copies indexed columns *c* times."),
			nameBuilder("roll", StandardVocabulary::roll)
				.indexer("[c=1,:]")
				.description("Permutes columns in stack *c* times."),
			nameBuilder("columns", StandardVocabulary::columns)
				.description("Adds a constant double column with the number of input columns."),
			nameBuilder("index", StandardVocabulary::index)
				.indexer("[c]")
				.description( "Indexes by constant double ordinals in c (start inclusive, end exclusive).  Assumes 0 for start if c is one constant."),

	///* dates */
			nameBuilder("today", StandardVocabulary::today)
				.indexer(Indexer.NONE)
				.description("Creates a constant date column with today's date."),
			nameBuilder("offset", StandardVocabulary::offset)
				.indexer("[date=today,periodicity=B,c=-1]")
				.description("Offset a *c* periods of *periodicity* from *date*."),
			nameBuilder("day_count", StandardVocabulary::dayCount)
				.indexer(Indexer.NONE)
				.description("Creates a column with count of days in each period."),
			nameBuilder("annualization_factor", StandardVocabulary::annualizationFactor)
				.indexer("[periodicity=\"range\"]")
				.description("Creates a column with annualization factor for the specified or evaluated range's periodicity."),
				
				
			nameBuilder("pi", StandardVocabulary::pi)
				.indexer(Indexer.NONE)
				.description("Creates a constant double column with the value pi."),
			nameBuilder("moon", StandardVocabulary::moon)
				.indexer(Indexer.NONE)
				.description("Creates a double column with values corresponding the phase of the moon."),
			nameBuilder("range", StandardVocabulary::range)
				.indexer("[c=1 4]")
				.description("Creates double columns corresponding to integers between first (inclusive) and second (exclusive) in *c*."),
			nameBuilder("read_csv", StandardVocabulary::readCsv)
				.indexer("[source,includes_header=\"true\"]")
				.description( "Reads CSV formatted table from file or URL specified as *source*."),
	

	/* data cleanup */
			nameBuilder("fill", StandardVocabulary::fill)
				.indexer("[periodicity=BQ-DEC,lookback=\"true\",:]")
				.description("Fills missing values with last good value, looking back one period of *freq* if *lookback* is true."),
			nameBuilder("join", StandardVocabulary::join)
				.indexer("[date,:]")
				.description("Joins columns over time, switching between columns on *dates*.\""),
			nameBuilder("resample", StandardVocabulary::resample)
				.indexer("[periodicity=BM,:]")
				.description("Sets frequency of prior columns to periodicity *freq*, carrying values forward if evaluation periodicity is more frequent."),
				
				
				
	/* header manipulation */
			nameBuilder("hcopy", StandardVocabulary::hcopy)
				.description("Copies headers to a comma-delimited constant string column."),
			nameBuilder("hpaste", StandardVocabulary::hpaste)
				.indexer("[string,repeat=\"true\",:]")
				.description("Sets headers of other input columns to the values in comma-delimited constant string column."),
			nameBuilder("hformat", StandardVocabulary::hformat)
				.indexer("[string,:]")
				.description("Formats headers, setting new value to *format* and substituting and occurences of \"{}\" with previous header value."),
				
				
				
	/* string manipulation */
			nameBuilder("cat", StandardVocabulary::cat)
				.indexer("[sep=\"\",:]")
				.description("Concatenates values of constant string columns."),
				
				
	/* array creation */
			nameBuilder("rolling", StandardVocabulary::rolling)
				.indexer("[c=2,periodicity=\"range\",:]")
				.description("Creates double array columns for each input column with rows containing values"
						+ "from rolling window of past data where the window is *c* periods of periodicity *periodicity*,"
						+ "defaulting to the evaluation periodicity."),
			nameBuilder("cross", StandardVocabulary::cross)
				.description("Creates a double array column with each row containing values of input columns."),
			nameBuilder("rev_expanding", StandardVocabulary::revExpanding)
				.description("Creates double array columns for each input column with rows containing values from the current period to the end of the range."),
			nameBuilder("expanding", StandardVocabulary::expanding)
				.indexer("[date=\"range\",periodicity=\"range\",initial_zero=\"false\",non_zero=\"false\",:]")
				.description("Creates double array columns for each input column with rows containing values from "
						+ "an expanding window of past data with periodicity *freq* that starts on date *start*."),
			nameBuilder("report", StandardVocabulary::report)
				.indexer("[HTML]")
				.terminal()
				.startFromLast()
				.description("Creates an HTML report from any columns labelled HTML."),
			nameBuilder("grid", StandardVocabulary::grid)
				.indexer("[columns=3,HTML]")
				.description("Creates a grid layout in a report with all input columns labelled HTML as cells in the grid."),
			nameBuilder("table", StandardVocabulary::table)
				.indexer("[periodicity=B, date=-20 offset today,format=\"decimal\",row_header=\"date\",col_headers=\"true\",:]")
				.description("Creates a const string column with code for a table defined by input columns and with header HTML."),
			nameBuilder("chart", StandardVocabulary::chart)
				.indexer("[periodicity=B, date=-20 offset today,title=\"\",options=\"\",:]")
				.description("Creates a const string column with code for an HTML chart."),
			nameBuilder("rt", StandardVocabulary::rt)
				.indexer("[functions=\" BA-DEC offset expanding pct_change {YTD} today today eval\",format=\"percent\",digits=2,:]")
				.description("Creates a const string column with code for an HTML ranking table, applying each *function* to input columns and ranking the results.")
		).stream().map(Name.Builder::build).collect(Collectors.toList());

		for(Entry<String, Supplier<Periodicity<?>>> e : Periodicities.map.entrySet()) {
			names.add(nameBuilder(e.getKey(), periodicityConstantFunction.apply(e.getValue().get()))
						.indexer(Indexer.NONE)
						.description( "Creates a constant periodcities column for " + e.getKey()).build());
		}

    	names.add(getCollectorName("first", DoubleCollector::first));
    	names.add(getCollectorName("last", DoubleCollector::last));
    	names.add(getCollectorName("change", DoubleCollector::change));
    	names.add(getCollectorName("pct_change", DoubleCollector::pct_change));
    	names.add(getCollectorName("log_change", DoubleCollector::log_change));
    	names.add(getCollectorName("count", DoubleCollector::count));
    	names.add(getCollectorName("mean", DoubleCollector::mean));
    	names.add(getCollectorName("geo_mean", DoubleCollector::geo_mean));
    	names.add(getCollectorName("sum", DoubleCollector::sum));
    	names.add(getCollectorName("max", DoubleCollector::max));
    	names.add(getCollectorName("min", DoubleCollector::min));
    	names.add(getCollectorName("varp", DoubleCollector::varp));
    	names.add(getCollectorName("var", DoubleCollector::var));
    	names.add(getCollectorName("std", DoubleCollector::std));
    	names.add(getCollectorName("stdp", DoubleCollector::stdp));
    	names.add(getCollectorName("zscore", DoubleCollector::zscore));
    	names.add(getCollectorName("zscorep", DoubleCollector::zscorep));

    	names.add(getBinaryOperatorName("+", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x + y));
    	names.add(getBinaryOperatorName("-", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x - y));
    	names.add(getBinaryOperatorName("*", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x * y));
    	names.add(getBinaryOperatorName("/", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x / y));
    	names.add(getBinaryOperatorName("%", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x % y));
    	names.add(getBinaryOperatorName("^", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : Math.pow(x, y)));
    	names.add(getBinaryOperatorName("==", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x == y ? 1.0 : 0.0));
    	names.add(getBinaryOperatorName("!=", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN :  x != y ? 1.0 : 0.0));
    	names.add(getBinaryOperatorName(">", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x > y ? 1.0 : 0.0));
    	names.add(getBinaryOperatorName("<", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x < y ? 1.0 : 0.0));
    	names.add(getBinaryOperatorName(">=", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x >= y ? 1.0 : 0.0));
    	names.add(getBinaryOperatorName("<=", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x <= y ? 1.0 : 0.0));

    	names.add(getUnaryOperatorName("abs", (DoubleUnaryOperator) Math::abs));
    	names.add(getUnaryOperatorName("sin", Math::sin));
    	names.add(getUnaryOperatorName("cos", Math::cos));
    	names.add(getUnaryOperatorName("tan", Math::tan));
    	names.add(getUnaryOperatorName("sqrt", Math::sqrt));
    	names.add(getUnaryOperatorName("log", Math::log));
    	names.add(getUnaryOperatorName("log10", Math::log10));
    	names.add(getUnaryOperatorName("exp", Math::exp));
    	names.add(getUnaryOperatorName("signum", (DoubleUnaryOperator)Math::signum));
    	names.add(getUnaryOperatorName("asin", Math::asin));
    	names.add(getUnaryOperatorName("acos", Math::acos));
    	names.add(getUnaryOperatorName("atan", Math::atan));
    	names.add(getUnaryOperatorName("toRadians", Math::toRadians));
    	names.add(getUnaryOperatorName("toDegrees", Math::toDegrees));
    	names.add(getUnaryOperatorName("cbrt", Math::cbrt));
    	names.add(getUnaryOperatorName("ceil", Math::ceil));
    	names.add(getUnaryOperatorName("floor", Math::floor));
    	names.add(getUnaryOperatorName("rint", Math::rint));
    	names.add(getUnaryOperatorName("ulp", (DoubleUnaryOperator)Math::ulp));
    	names.add(getUnaryOperatorName("sinh", Math::sinh));
    	names.add(getUnaryOperatorName("cosh", Math::cosh));
    	names.add(getUnaryOperatorName("tanh", Math::tanh));
    	names.add(getUnaryOperatorName("expm1", Math::expm1));
    	names.add(getUnaryOperatorName("log1p", Math::log1p));
    	names.add(getUnaryOperatorName("nextUp", (DoubleUnaryOperator)Math::nextUp));
    	names.add(getUnaryOperatorName("nextDown", (DoubleUnaryOperator)Math::nextDown));
    	names.add(getUnaryOperatorName("neg", x -> x * -1.0d));
    	names.add(getUnaryOperatorName("inv", x -> 1.0 / x));
    	names.add(getUnaryOperatorName("zeroToNa", x -> x == 0 ? Double.NaN : x));
		names.add(getUnaryOperatorName("xmPrice", quote -> {
			double TERM = 10, RATE = 6, price = 0;
			for (int i = 0; i < TERM * 2; i++) {
				price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
			}
			return price + 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
		}));
		names.add(getUnaryOperatorName("ymPrice", quote -> {
			double TERM = 3, RATE = 6, price = 0;
			for (int i = 0; i < TERM * 2; i++) {
				price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
			}
			return price + 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
		}));

	}

	private static final void def(Pinto p, Table t) {
		Pinto.Expression e = p.getExpression();
		String name = e.getNameLiteral()
				.orElseThrow(() -> new PintoSyntaxException("def requires a name literal."));
		p.getNamespace().define(p,name, e.getDefinedIndexer(), e.getText(), e.getDependencies(), e.getPrevious());
		t.setStatus("Defined " + name);
	}

	private static void undef(Pinto p, Table t) {
		String name = p.getExpression().getNameLiteral()
				.orElseThrow(() -> new PintoSyntaxException("del requires a name literal."));
		p.getNamespace().undefine(name);
		t.setStatus("Undefined " + name);
	}

	private static void help(Pinto p, Table t) {
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		if (p.getExpression().getNameLiteral().isPresent()) {
			String n = p.getExpression().getNameLiteral().get();
			sb.append(p.getNamespace().getName(n).getHelp(n)).append(crlf);
		} else {
			sb.append("Pinto help").append(crlf);
			sb.append("Built-in names:").append(crlf);
			List<String> l = new ArrayList<>(p.getNamespace().getNames());
			List<String> l2 = new ArrayList<>();
			for (int i = 0; i < l.size(); i++) {
				if (p.getNamespace().getName(l.get(i)).builtIn()) {
					sb.append(l.get(i)).append(i == l.size() - 1 || i > 0 && i % 7 == 0 ? crlf : ", ");
				} else {
					l2.add(l.get(i));
				}
			}
			sb.append("Defined names:").append(crlf);
			for (int i = 0; i < l2.size(); i++) {
				sb.append(l2.get(i)).append(i == l2.size() - 1 || i > 0 && i % 7 == 0 ? crlf : ", ");
			}
			sb.append(crlf).append("For help with a specific function type \":function help\"").append(crlf);
		}
		t.setStatus(sb.toString());
	}

	private static void list(Pinto p, Table t) {
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		p.getNamespace().getNames().stream().map(s -> p.getNamespace().getName(s)).forEach(name -> {
			sb.append(name.toString()).append("|").append(name.getIndexString()).append("|")
					.append(name.getDescription()).append(crlf);
		});
		t.setStatus(sb.toString());
	}

	private static void eval(Pinto p, Table t) {
		LinkedList<Column<?>> s = t.flatten();
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		LinkedList<LocalDate> dates = new LinkedList<>();
		while ((!s.isEmpty()) && dates.size() < 2 && s.peekFirst().getHeader().equals("date")) {
			dates.add(((Column.OfConstantDates) s.removeFirst()).getValue());
		}
		t.evaluate(periodicity.range(dates.removeLast(), dates.isEmpty() ? LocalDate.now() : dates.peek()));
	}

	private static void imp(Pinto p, Table t) {
		for (LinkedList<Column<?>> s : t.takeTop()) {
			while (!s.isEmpty()) {
				String filename = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
				int lineNumber = 0;
				try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
					String line = null;
					p.activeExpression.reset();
					while ((line = reader.readLine()) != null) {
						lineNumber++;
						p.eval(line);
					}
				} catch (FileNotFoundException e) {
					throw new IllegalArgumentException("Cannot find pinto file \"" + filename + "\" to execute");
				} catch (IOException e1) {
					throw new IllegalArgumentException("IO error for pinto file \"" + filename + "\" in execute");
				} catch (PintoSyntaxException e) {
					throw new IllegalArgumentException(
							"Pinto syntax error in  file \"" + filename + "\" at line: " + Integer.toString(lineNumber),
							e);
				}
			}
		}
		t.setStatus("Successfully executed");
	}

	private static void to_csv(Pinto p, Table t) {
		LinkedList<Column<?>> s = t.flatten();
		String filename = castColumn(s.removeFirst(),OfConstantStrings.class).getValue();
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		LinkedList<LocalDate> dates = new LinkedList<>();
		while ((!s.isEmpty()) && dates.size() < 2 && s.peekFirst().getHeader().equals("date")) {
			dates.add(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		}
		t.evaluate(periodicity.range(dates.removeLast(), dates.isEmpty() ? LocalDate.now() : dates.peek()));
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
			out.print(t.toCsv());
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to open file \"" + filename + "\" for export");
		}
		t.setStatus("Successfully exported");
	}

	private static void only(Pinto p, Table t) {
		List<LinkedList<Column<?>>> indexed = t.takeTop();
		t.clearTop();
		t.insertAtTop(indexed);
	}

	private static void clear(Pinto p, LinkedList<Column<?>> s) {
		s.clear();
	}

	private static void rev(Pinto p, LinkedList<Column<?>> s) {
		Collections.reverse(s);
	}

	private static void pull(Pinto p, LinkedList<Column<?>> s) {
		;
	}

	private static void copy(Pinto p, LinkedList<Column<?>> s) {
		int times = (int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
		LinkedList<Column<?>> temp = new LinkedList<>();
		s.stream().forEach(temp::addFirst);
		for (int j = 0; j < times - 1; j++) {
			temp.stream().map(Column::clone).forEach(s::addFirst);
		}
	}

	private static void roll(Pinto p, LinkedList<Column<?>> s) {
		int times = (int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
		for (int j = 0; j < times; j++) {
			s.addLast(s.removeFirst());
		}
	}

	private static void columns(Pinto p, LinkedList<Column<?>> s) {
		s.addFirst(new OfConstantDoubles(s.size()));
	}

	private static void index(Pinto p, Table t) {
		LinkedList<Column<?>> s = t.takeTop().get(0);
		List<String> endpoints = new LinkedList<>();
		while (!s.isEmpty() && endpoints.size() < 2 && s.peekFirst().getHeader().equals("c")) {
			endpoints
					.add(Integer.toString((int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue()));
		}
		t.insertAtTop(s);
		String index = endpoints.size() == 1 ? ":" + endpoints.get(0) : endpoints.get(1) + ":" + endpoints.get(0);
		new Indexer(index, false).apply(p).accept(t);
	}

	private static void today(Pinto p, LinkedList<Column<?>> s) {
		s.addFirst(new OfConstantDates(LocalDate.now()));
	}

	private static void offset(Pinto p, LinkedList<Column<?>> s) {
		LocalDate date = castColumn(s.removeFirst(), OfConstantDates.class).getValue();
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		int count = castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		s.addFirst(new OfConstantDates(periodicity.offset(count, date)));
	}

	private static void dayCount(Pinto p, LinkedList<Column<?>> s) {
		s.addFirst(new OfDoubles(c -> "day_count", StandardVocabulary::dayCountRowFunction));
	}

	private static <P extends Period<P>> double[] dayCountRowFunction(PeriodicRange<P> range, Column<?>[] inputs,
			Class<?> c) {
		double[] d = new double[(int)range.size()];
		List<P> l = range.values();
		for(int i = 0; i < d.length; i++) {
			d[i] = l.get(i).dayCount();
		}
		return d;
	}

	private static void annualizationFactor(Pinto pinto, LinkedList<Column<?>> s) {
		Optional<Periodicity<?>> periodicity = s.peekFirst() instanceof Column.OfConstantStrings
				&& castColumn(s.removeFirst(), OfConstantStrings.class).getValue().equals("range") ? Optional.empty()
						: Optional.of(castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue());
		Function<Optional<Periodicity<?>>, RowsFunction<double[]>> function = p -> (r, c) -> {
			double[] a = new double[(int) r.size()];
			Arrays.fill(a, p.orElse(r.periodicity()).annualizationFactor());
			return a;
		};
		s.addFirst(new OfDoubles(c -> "annualization_factor", function.apply(periodicity)));
	}

	private static void pi(Pinto p, LinkedList<Column<?>> s) {
		s.addFirst(new OfConstantDoubles(Math.PI));
	}

	private static void moon(Pinto p, LinkedList<Column<?>> s) {
		s.addFirst(new OfDoubles(i -> "moon", StandardVocabulary::moonRowFunction));
	}

	private static double[] moonRowFunction(PeriodicRange<?> range, Column<?>[] inputs) {
		return range.dates().stream().mapToDouble(d -> {
			return new tech.pinto.tools.MoonPhase(d).getPhase();
		}).toArray();
	}

	private static void range(Pinto p, LinkedList<Column<?>> s) {
		LinkedList<Integer> endpoints = new LinkedList<Integer>();
		while (!s.isEmpty() && endpoints.size() < 2 && s.peekFirst().getHeader().equals("c")) {
			endpoints.addLast((int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue());
		}
		int start = endpoints.size() == 2 ? endpoints.removeLast() : 1;
		int end = endpoints.removeFirst();
		IntStream.range(start, end).mapToDouble(i -> (double) i).mapToObj(value -> new OfConstantDoubles(value))
				.forEach(s::addFirst);
	}

	private static void readCsv(Pinto p, LinkedList<Column<?>> s) {
		String source = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
		boolean includesHeader = Boolean.parseBoolean(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		try {
			List<String> lines = null;
			if (!source.contains("http")) {
				lines = Files.readAllLines(Paths.get(source));
			} else {
				lines = new ArrayList<>();
				BufferedReader in = new BufferedReader(new InputStreamReader(new URL(source).openStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null)
					lines.add(inputLine);
				in.close();
			}
			if (lines.size() > 0) {
				String[] firstRow = lines.get(0).split(",");
				final String[] labels = includesHeader ? Arrays.copyOfRange(firstRow, 1, firstRow.length)
						: new String[firstRow.length];
				final Map<LocalDate, String[]> data = lines.stream().skip(includesHeader ? 1 : 0)
						.map(line -> line.split(","))
						.collect(Collectors.toMap((r) -> LocalDate.parse(r[0]), Function.identity()));
				for (int i = 0; i < firstRow.length - 1; i++) {
					final int col = i;
					s.add(new OfDoubles(inputs -> labels[col] != null ? labels[col] : "", (range, inputs) -> {
						DoubleStream.Builder b = DoubleStream.builder();
						for (Period<?> per : range.values()) {
							if (data.containsKey(per.endDate())) {
								try {
									b.accept(Double.valueOf(data.get(per.endDate())[col + 1]));
								} catch (NumberFormatException nfe) {
									b.accept(Double.NaN);
								}
							} else {
								b.accept(Double.NaN);
							}
						}
						return b.build().toArray();

					}));
				}
			}
		} catch (IOException e) {
			throw new PintoSyntaxException("Unable to import file \"" + source + "\".", e);
		}
	}

	private static void fill(Pinto pinto, LinkedList<Column<?>> s) {
		Periodicity<?> p = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		boolean lb = Boolean.parseBoolean(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		Function<Periodicity<?>, Function<Boolean, RowsFunctionGeneric<double[]>>> fillRowFunction = periodicity -> lookBack -> {
			return new RowsFunctionGeneric<double[]>() {
				@Override
				public <P extends Period<P>> double[] getRows(PeriodicRange<P> range, Column<?>[] inputs,
						Class<?> clazz) {
					int skip = 0;
					PeriodicRange<?> r = range;
					if (lookBack) {
						r = range.periodicity().range(periodicity.previous(range.start().endDate()).endDate(),
								range.end().endDate());
						skip = (int) r.indexOf(range.start().endDate());
					}
					double[] input = castColumn(inputs[0], OfDoubles.class).rows(r);
					double[] output = new double[(int) range.size()];
					int i = skip;
					while (i > 0 && Double.isNaN(input[i])) {
						i--;
					}
					output[0] = input[i];
					for (i = 1; i < output.length; i++) {
						output[i] = Double.isNaN(input[i + skip]) ? output[i - 1] : input[i + skip];
					}
					return output;
				}
			};
		};
		s.replaceAll(oldColumn -> {
			return new OfDoubles(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " fill",
					fillRowFunction.apply(p).apply(lb), oldColumn);
		});
	}

	private static void join(Pinto pinto, LinkedList<Column<?>> s) {
		LinkedList<LocalDate> cutoverDates = new LinkedList<>();
		while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("date")) {
			cutoverDates.add(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		}
		OfDoubles[] inputStack = s.toArray(new OfDoubles[] {});
		s.clear();
		Function<LinkedList<LocalDate>, RowsFunctionGeneric<double[]>> joinRowFunction = cd -> {
			return new RowsFunctionGeneric<double[]>() {
				@Override
				public <P extends Period<P>> double[] getRows(PeriodicRange<P> range, Column<?>[] inputArray,
						Class<?> clazz) {
					double[] output = new double[(int) range.size()];
					int outputIndex = 0;
					LinkedList<Column<?>> inputs = new LinkedList<>(Arrays.asList(inputArray));
					Collections.reverse(inputs);
					List<P> cutoverPeriods = cd.stream().map(range.periodicity()::from).collect(Collectors.toList());
					Collections.sort(cutoverPeriods);
					P current = range.start();
					Periodicity<P> freq = range.periodicity();
					int i = 0;
					while (i < cutoverPeriods.size() && !current.isAfter(range.end())) {
						if (inputs.isEmpty()) {
							throw new PintoSyntaxException("Not enough columns to join on " + cd.size() + " dates.");
						}
						OfDoubles currentFunction = castColumn(inputs.removeFirst(), OfDoubles.class);
						if (current.isBefore(cutoverPeriods.get(i))) {
							P chunkEnd = range.end().isBefore(cutoverPeriods.get(i)) ? range.end()
									: freq.previous(cutoverPeriods.get(i));
							double[] d = currentFunction.rows(freq.range(current, chunkEnd));
							System.arraycopy(d, 0, output, outputIndex, d.length);
							outputIndex += d.length;
							current = freq.next(chunkEnd);
						}
						i++;
					}
					if (inputs.isEmpty()) {
						throw new IllegalArgumentException("Not enough columns to join on " + cd.size() + " dates.");
					}
					OfDoubles currentFunction =  castColumn(inputs.removeFirst(), OfDoubles.class);
					if (!current.isAfter(range.end())) {
						double[] d = currentFunction.rows(range.periodicity().range(current, range.end()));
						System.arraycopy(d, 0, output, outputIndex, d.length);
					}
					return output;
				}
			};
		};
		s.add(new OfDoubles(i -> i[0].getHeader(),
				i -> Arrays.stream(i).map(Column::getTrace).collect(Collectors.joining(" ")) + " join",
				joinRowFunction.apply(cutoverDates), inputStack));
	}

	private static void resample(Pinto pinto, LinkedList<Column<?>> s) {
		Periodicity<?> newPeriodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		s.replaceAll(c -> {
			return new OfDoubles(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + newPeriodicity.code() + " resample",
					getResampleFunction(newPeriodicity), c);
		});
	}

	private static <N extends Period<N>> RowsFunctionGeneric<double[]> getResampleFunction(Periodicity<N> p) {
		return new RowsFunctionGeneric<double[]>() {
			@Override
			public <P extends Period<P>> double[] getRows(PeriodicRange<P> range, Column<?>[] inputs, Class<?> clazz) {
				N newStart = p.roundDown(range.start().endDate());
				if (newStart.endDate().isAfter(range.start().endDate())) {
					newStart = newStart.previous();
				}
				N newEnd = p.from(range.end().endDate());
				PeriodicRange<N> newDr = p.range(newStart, newEnd);
				double[] d = (double[]) inputs[0].rows(newDr);
				double[] output = new double[(int) range.size()];
				List<P> l = range.values();
				for (int i = 0; i < output.length; i++) {
					output[i] = d[(int) newDr.indexOf(p.roundDown(l.get(i).endDate()))];
				}
				return output;
			}
		};
	}

	private static void hcopy(Pinto pinto, LinkedList<Column<?>> s) {
		List<String> headers = s.stream().map(Column::getHeader).collect(Collectors.toList());
		Collections.reverse(headers);
		s.addFirst(new OfConstantStrings(headers.stream().collect(Collectors.joining(","))));
	}

	private static void hpaste(Pinto pinto, LinkedList<Column<?>> s) {
		String[] headers = castColumn(s.removeFirst(), OfConstantStrings.class).getValue().split(",");
		boolean repeat = Boolean.parseBoolean(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		AtomicInteger i = new AtomicInteger(headers.length - 1);
		s.replaceAll(c -> {
			int index = i.getAndDecrement();
			if (index >= 0 || repeat) {
				c.setHeader(headers[index <= 0 ? 0 : index]);
			}
			return c;
		});
	}

	private static void hformat(Pinto pinto, LinkedList<Column<?>> s) {
		MessageFormat format = new MessageFormat(
				castColumn(s.removeFirst(), OfConstantStrings.class).getValue().replaceAll("\\{\\}", "\\{0\\}"));
		s.replaceAll(c -> {
			String header = format.format(new Object[] { c.getHeader() });
			c.setHeader(header);
			return c;
		});
	}

	private static void cat(Pinto pinto, LinkedList<Column<?>> s) {
		String sep = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
		List<String> l = new ArrayList<>();
		while (!s.isEmpty()) {
			Column<?> c = s.removeFirst();
			if (c instanceof ConstantColumn) {
				l.add(((ConstantColumn<?>) c).getValue().toString());
			} else {
				throw new IllegalArgumentException("cat can only operate on constant columns.");
			}
		}
		s.addFirst(new OfConstantStrings(l.stream().collect(Collectors.joining(sep))));
	}

	private static void rolling(Pinto pinto, LinkedList<Column<?>> s) {
		int size = (int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
		Periodicity<?> windowFreq = s.peekFirst() instanceof OfConstantStrings
				&& castColumn(s.removeFirst(), OfConstantStrings.class).getValue().equals("range") ? null
						: castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		s.replaceAll(c -> {
			return new OfDoubleArray1Ds(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + " rolling", getRollingFunction(windowFreq).apply(size), c);
		});

	}

	private static <N extends Period<N>> Function<Integer, RowsFunctionGeneric<double[][]>> getRollingFunction(
			Periodicity<N> n) {
		return size -> new RowsFunctionGeneric<double[][]>() {
			@Override
			public <P extends Period<P>> double[][] getRows(PeriodicRange<P> range, Column<?>[] inputs,
					Class<?> clazz) {
				double[][] output = new double[(int) range.size()][];
				if (n != null) {
					N expandedWindowStart = n.offset(-1 * (size - 1), n.from(range.start().endDate()));
					N windowEnd = n.from(range.end().endDate());
					PeriodicRange<N> expandedWindow = n.range(expandedWindowStart, windowEnd);
					double[] data = castColumn(inputs[0], OfDoubles.class).rows(expandedWindow);
					List<P> l = range.values();
					for (int i = 0; i < output.length; i++) {
						long windowStartIndex = n.distance(expandedWindowStart, n.from(l.get(i).endDate())) - size + 1;
						output[i] = Arrays.copyOfRange(data, (int) windowStartIndex, (int) windowStartIndex + size);
					}
					return output;
				} else {
					P expandedWindowStart = range.periodicity().offset(-1 * (size - 1), range.start());
					PeriodicRange<P> expandedWindow = range.periodicity().range(expandedWindowStart, range.end());
					double[] data = castColumn(inputs[0], OfDoubles.class).rows(expandedWindow);
					for (int i = 0; i < output.length; i++) {
						output[i] = Arrays.copyOfRange(data, i, i + size);
					}
					return output;
				}
			}
		};
	}

	private static void cross(Pinto pinto, LinkedList<Column<?>> s) {
		Column.OfDoubles[] a = s.toArray(new OfDoubles[] {});
		s.clear();
		s.add(new OfDoubleArray1Ds(inputs -> "cross", inputs -> "cross", StandardVocabulary::crossRowFunction,
				a));
	}

	private static double[][] crossRowFunction(PeriodicRange<?> range, Column<?>[] columns) {
		double[][] output = new double[(int) range.size()][columns.length];
		double[][] input = new double[columns.length][];
		for (int j = 0; j < columns.length; j++) {
			input[j] = castColumn(columns[j], OfDoubles.class).rows(range);
		}
		for (int j = 0; j < columns.length; j++) {
			for (int i = 0; i < output.length; i++) {
				output[i][j] = input[j][i];
			}
		}
		return output;
	}

	private static void revExpanding(Pinto pinto, LinkedList<Column<?>> s) {
		s.replaceAll(c -> {
			return new OfDoubleArray1Ds(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + " rev_expanding", StandardVocabulary::revExpandingRowFunction, c);
		});
	}

	private static double[][] revExpandingRowFunction(PeriodicRange<?> range, Column<?>[] inputs) {
		double[][] output = new double[(int) range.size()][];
		double[] data = castColumn(inputs[0], OfDoubles.class).rows(range);
		for (int i = 0; i < range.size(); i++) {
			output[i] = Arrays.copyOfRange(data, i, data.length);
		}
		return output;
	}

	private static void expanding(Pinto pinto, LinkedList<Column<?>> s) {
		Optional<LocalDate> start = s.peekFirst() instanceof OfConstantStrings
				&& castColumn(s.removeFirst(), OfConstantStrings.class).getValue().equals("range") ? Optional.empty()
						: Optional.of(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		Periodicity<?> periodicity = s.peekFirst() instanceof OfConstantStrings
				&& castColumn(s.removeFirst(), OfConstantStrings.class).getValue().equals("range") ? null
						: castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		boolean initialZero = Boolean.parseBoolean(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		boolean nonZero = Boolean.parseBoolean(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		s.replaceAll(c -> new OfDoubleArray1Ds(inputs -> inputs[0].getHeader(),
				inputs -> inputs[0].getTrace() + " expanding",
				getExpandingFunction(periodicity).apply(start).apply(initialZero).apply(nonZero), c));
	}

	private static <N extends Period<N>> Function<Optional<LocalDate>, Function<Boolean, Function<Boolean, RowsFunctionGeneric<double[][]>>>> getExpandingFunction(
			Periodicity<N> n) {
		return start -> initialZero -> nonZero -> new RowsFunctionGeneric<double[][]>() {
			@Override
			public <P extends Period<P>> double[][] getRows(PeriodicRange<P> range, Column<?>[] inputs,
					Class<?> clazz) {
				double[][] output = new double[(int) range.size()][];
				LocalDate startDate = start.orElse(range.start().endDate());
				boolean rangePeriodicity = n == null;
				double[] data = null;
				PeriodicRange<N> newWindow = null;
				PeriodicRange<P> rangeWindow = null;
				if (!rangePeriodicity) {
					N windowStart = n.from(startDate);
					N windowEnd = n.from(range.end().endDate());
					windowEnd = windowEnd.isBefore(windowStart) ? windowStart : windowEnd;
					newWindow = n.range(windowStart, windowEnd);
					data = castColumn(inputs[0], OfDoubles.class).rows(newWindow);
				} else {
					P windowStart = range.periodicity().from(startDate);
					P windowEnd = range.end().isBefore(windowStart) ? windowStart : range.end();
					rangeWindow = range.periodicity().range(windowStart, windowEnd);
					data = castColumn(inputs[0], OfDoubles.class).rows(rangeWindow);
				}
				if (initialZero) {
					data[0] = 0.0d;
				}
				List<P> rangePeriods = range.values();
				for (int i = 0; i < range.size(); i++) {
					int index = rangePeriodicity ? (int) rangeWindow.indexOf(rangePeriods.get(i))
							: (int) newWindow.indexOf(rangePeriods.get(i).endDate());
					if (index >= 0) {
						int startIndex = 0;
						if (nonZero) {
							startIndex = index + 1;
							while (startIndex > 0 && data[startIndex - 1] != 0) {
								startIndex--;
							}
						}
						output[i] = Arrays.copyOfRange(data, startIndex, index + 1);
					} else {
						output[i] = new double[] { Double.NaN };
					}
				}
				return output;
			}
		};
	}

	private static void report(Pinto p, Table t) {
		Pinto.Expression state = p.getExpression();
		String id = getId();
		p.getNamespace().define(p,"_rpt-" + id, Indexer.NONE, "Report id: " + id, state.getDependencies(), state.getPrevious());
		try {
			int port = p.getPort();
			Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + port + "/pinto/report?p=" + id));
		} catch (Exception e) {
			throw new PintoSyntaxException("Unable to open report", e);
		}
		t.setStatus("Report id: " + id);
	}

	private static void chart(Pinto pinto, LinkedList<Column<?>> s) {
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		LinkedList<LocalDate> d = new LinkedList<>();
		while ((!s.isEmpty()) && d.size() < 2 && s.peekFirst().getHeader().equals("date")) {
			d.add(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		}
		PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek());
		String title = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
		String options = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
		if (!chartHTML.isPresent()) {
			try {
				chartHTML = Optional.of(new MessageFormat(readInputStreamIntoString(
						(StandardVocabulary.class.getClassLoader().getResourceAsStream("report_chart.html")))));
			} catch (IOException e) {
				throw new PintoSyntaxException("Unable to open chart html template", e);
			}
		}
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(14);
		String dates = range.dates().stream().map(LocalDate::toString)
				.collect(Collectors.joining("', '", "['x', '", "']"));
		StringBuilder data = new StringBuilder();
		for (int i = s.size() - 1; i >= 0; i--) {
			data.append("['").append(s.get(i).getHeader()).append("',");
			data.append(castColumn(s.get(i), OfDoubles.class).rowsAsCsv(range, nf)).append("]");
			data.append(i > 0 ? "," : "");
		}
		String html = chartHTML.get()
				.format(new Object[] { getId(), dates, data, title, options }, new StringBuffer(), null).toString();
		s.clear();
		s.add(new OfConstantStrings(html, "HTML"));

	}

	private static void grid(Pinto pinto, LinkedList<Column<?>> s) {
		int columns = Double.valueOf(castColumn(s.removeFirst(), OfConstantDoubles.class).getValue()).intValue();
		StringBuilder sb = new StringBuilder();
		sb.append("\n<table class=\"pintoGrid\">\n\t<tbody>\n");
		for (int i = 0; i < s.size();) {
			if (i % columns == 0) {
				sb.append("\t\t<tr>\n");
			}
			sb.append("\t\t\t<td>").append(castColumn(s.get(s.size() - i++ - 1), OfConstantStrings.class).getValue())
					.append("</td>\n");
			if (i % columns == 0 || i == s.size()) {
				sb.append("\t\t</tr>\n");
			}
		}
		sb.append("\t</tbody>\n</table>\n");
		s.clear();
		s.add(new OfConstantStrings(sb.toString(), "HTML"));
	}

	private static void table(Pinto pinto, LinkedList<Column<?>> s) {
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		LinkedList<LocalDate> d = new LinkedList<>();
		while ((!s.isEmpty()) && d.size() < 2 && s.peekFirst().getHeader().equals("date")) {
			d.add(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		}
		PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek());
		NumberFormat nf = castColumn(s.removeFirst(), OfConstantStrings.class).getValue().equals("percent")
				? NumberFormat.getPercentInstance()
				: NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(4);
		nf.setGroupingUsed(false);
		String rowHeader = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
		boolean colHeaders = Boolean.parseBoolean(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		Table t = new Table();
		t.insertAtTop(s);
		t.evaluate(range);
		String[] lines = t.toCsv(nf).split("\n");
		if (!rowHeader.equals("date")) {
			for (int i = 0; i < lines.length; i++) {
				String[] cols = lines[i].split(",");
				cols[0] = i == 0 ? "" : rowHeader;
				lines[i] = Arrays.stream(cols).collect(Collectors.joining(","));
			}
		}
		if (!colHeaders) {
			lines[0] = lines[0].replaceAll("[^,]", "&nbsp;");
		}
		s.clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"pintoTable\">\n<thead>\n");
		Arrays.stream(lines[0].split(","))
				.forEach(h -> sb.append("\t<th class=\"rankingTableHeader\">").append(h).append("</th>\n"));
		sb.append("</thead>\n<tbody>\n");
		Arrays.stream(lines).skip(1).map(l -> l.split(",")).forEach(l -> {
			sb.append("<tr>\n");
			Arrays.stream(l).forEach(c -> sb.append("<td>").append(c).append("</td>"));
			sb.append("</tr>\n");
		});
		sb.append("</tbody></table>\n");
		s.add(new OfConstantStrings(sb.toString(), "HTML"));
	}

	private static void rt(Pinto pinto, LinkedList<Column<?>> s) {
		LinkedList<String> functions = new LinkedList<>();
		while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("functions")) {
			functions.addFirst(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		}
		NumberFormat nf = castColumn(s.removeFirst(), OfConstantStrings.class).getValue().equals("percent")
				? NumberFormat.getPercentInstance()
				: NumberFormat.getNumberInstance();
		int digits = castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		nf.setMaximumFractionDigits(digits);
		int columns = functions.size();
		int rows = s.size();
		String[] labels = new String[rows];
		String[] headers = new String[columns];
		String[][] cells = new String[rows][columns];
		for (int i = 0; i < columns; i++) {
			double[][] values = new double[s.size()][2];
			for (int j = 0; j < s.size(); j++) {
				Pinto.Expression state = new Pinto.Expression(false);
				final int J = j;
				state.setCurrent(t -> {
					LinkedList<Column<?>> stack = new LinkedList<>();
					stack.add(s.get(J).clone());
					t.insertAtTop(stack);
				});
				Table t = pinto.evaluate(functions.get(i), state).get(0);
				double[][] d = t.toColumnMajorArray();
				values[j][0] = d[0][d[0].length - 1];
				values[j][1] = (int) j;
				if (j == 0) {
					headers[i] = t.getHeaders(false, false).get(0);
				}
				if (i == 0) {
					labels[j] = s.get(j).getHeader();
				}
			}

			Arrays.sort(values, (c1, c2) -> c1[0] == c2[0] ? 0 : c1[0] < c2[0] ? 1 : -1);
			for (int j = 0; j < values.length; j++) {
				StringBuilder sb = new StringBuilder();
				sb.append("\t<td id=\"rankingColor").append((int) values[j][1]).append("\" class=\"rankingTableCell\">")
						.append(labels[(int) values[j][1]]).append(": ").append(nf.format(values[j][0]))
						.append("</td>\n");
				cells[j][i] = sb.toString();
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"rankingTable\">\n<thead>\n");
		Arrays.stream(headers)
				.forEach(h -> sb.append("\t<th class=\"rankingTableHeader\">").append(h).append("</th>\n"));
		sb.append("</thead>\n<tbody>\n");
		for (int i = 0; i < cells.length; i++) {
			sb.append("<tr>\n");
			for (int j = 0; j < columns; j++) {
				sb.append(cells[i][j]);
			}
			sb.append("</tr>\n");
		}
		sb.append("</tbody></table>\n");
		s.clear();
		s.add(new OfConstantStrings(sb.toString(), "HTML"));

	}

	private static Function<Periodicity<?>, StackFunction> periodicityConstantFunction = per -> (p, s) -> s
			.addFirst(new Column.OfConstantPeriodicities(per));

	private static Name getBinaryOperatorName(String name, DoubleBinaryOperator dbo) {
		Function<DoubleBinaryOperator, RowsFunction<double[]>> doubledouble = o -> (range, inputs) -> {
			double[] l = castColumn(inputs[1], Column.OfDoubles.class).rows(range);
			double[] r = castColumn(inputs[0], Column.OfDoubles.class).rows(range);
			for (int j = 0; j < l.length; j++) {
				l[j] = o.applyAsDouble(l[j], r[j]);
			}
			return l;
		};
		Function<DoubleBinaryOperator, RowsFunction<double[][]>> arrayarray = o -> (range, inputs) -> {
			double[][] l = castColumn(inputs[1], OfDoubleArray1Ds.class).rows(range);
			double[][] r = castColumn(inputs[0], OfDoubleArray1Ds.class).rows(range);
			for (int x = 0; x < l.length; x++) {
				if (l[x].length != r[x].length) {
					throw new IllegalArgumentException("Length mismatch to apply " + name + " for array values.");
				}
				for (int y = 0; y < l[x].length; y++) {
					l[x][y] = o.applyAsDouble(l[x][y], r[x][y]);
				}
			}
			return l;
		};
		Function<DoubleBinaryOperator, RowsFunction<double[][]>> doublearray = o -> (range, inputs) -> {
			double[][] r = castColumn(inputs[0], OfDoubleArray1Ds.class).rows(range);
			double[] l = castColumn(inputs[1], OfDoubles.class).rows(range);
			for (int x = 0; x < l.length; x++) {
				for (int y = 0; y < r[x].length; y++) {
					r[x][y] = o.applyAsDouble(r[x][y], l[x]);
				}
			}
			return r;
		};
		Function<DoubleBinaryOperator, RowsFunction<double[][]>> arraydouble = o -> (range, inputs) -> {
			double[][] l = castColumn(inputs[1], OfDoubleArray1Ds.class).rows(range);
			double[] r = castColumn(inputs[0], OfDoubles.class).rows(range);
			for (int x = 0; x < l.length; x++) {
				for (int y = 0; y < l[x].length; y++) {
					l[x][y] = o.applyAsDouble(l[x][y], r[x]);
				}
			}
			return l;
		};
		Function<DoubleBinaryOperator, StackFunction> function = dc -> (p, stack) -> {
			int rightCount = (int) castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
			if (stack.size() < rightCount + 1) {
				throw new IllegalArgumentException("Not enough inputs for " + name);
			}
			LinkedList<Column<?>> rights = new LinkedList<>(stack.subList(0, rightCount));
			List<Column<?>> lefts = new ArrayList<>(stack.subList(rightCount, stack.size()));
			stack.clear();
			for (int i = 0; i < lefts.size(); i++) {
				Column<?> right = i >= rights.size() ? rights.getFirst().clone() : rights.getFirst();
				rights.addLast(rights.removeFirst());
				if (right instanceof OfConstantDoubles && lefts.get(i) instanceof OfConstantDoubles) {
					stack.add(new OfConstantDoubles(
							dc.applyAsDouble(castColumn(lefts.get(i), OfConstantDoubles.class).getValue(),
									castColumn(right, OfConstantDoubles.class).getValue())));
				} else if (right instanceof OfDoubles && lefts.get(i) instanceof OfDoubles) {
					stack.add(new OfDoubles(inputs -> inputs[1].getHeader(),
							inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
							doubledouble.apply(dc), right, lefts.get(i)));
				} else if (right instanceof OfDoubleArray1Ds
						&& lefts.get(i) instanceof OfDoubleArray1Ds) {
					stack.add(new OfDoubleArray1Ds(inputs -> inputs[1].getHeader(),
							inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
							arrayarray.apply(dc), right, lefts.get(i)));
				} else if (right instanceof OfDoubles && lefts.get(i) instanceof OfDoubleArray1Ds) {
					stack.add(new OfDoubleArray1Ds(inputs -> inputs[1].getHeader(),
							inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
							arraydouble.apply(dc), right, lefts.get(i)));
				} else if (right instanceof OfDoubleArray1Ds && lefts.get(i) instanceof OfDoubles) {
					stack.add(new OfDoubleArray1Ds(inputs -> inputs[1].getHeader(),
							inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
							doublearray.apply(dc), right, lefts.get(i)));
				} else {
					throw new IllegalArgumentException(
							"Operator " + name + " can only operate on columns of doubles or double arrays.");
				}
			}

		};
		return nameBuilder(name, function.apply(dbo))
				.description("Binary operator " + name
						+ " that operates on *width* columns at a time with fixed right-side operand.")
				.indexer("[width=1,:]").build();
	}

	private static Name getUnaryOperatorName(String name, DoubleUnaryOperator duo) {
		Function<DoubleUnaryOperator, RowsFunction<double[]>> doubleFunction = o -> (range, inputs) -> {
			double d[] = castColumn(inputs[0], OfDoubles.class).rows(range);
			for (int i = 0; i < d.length; i++) {
				d[i] = o.applyAsDouble(d[i]);
			}
			return d;
		};
		Function<DoubleUnaryOperator, RowsFunction<double[][]>> arrayFunction = o -> (range, inputs) -> {
			double d[][] = castColumn(inputs[0], OfDoubleArray1Ds.class).rows(range);
			for (int i = 0; i < d.length; i++) {
				for (int j = 0; j < d[i].length; j++) {
					d[i][j] = o.applyAsDouble(d[i][j]);
				}
			}
			return d;
		};
		Function<DoubleUnaryOperator, StackFunction> function = dc -> (p, s) -> {
			s.replaceAll(c -> {
				if (c instanceof OfDoubles) {
					return new OfDoubles(inputs -> inputs[0].getHeader(),
							inputs -> inputs[0].getTrace() + " " + name, doubleFunction.apply(duo), c);
				} else if (c instanceof OfDoubleArray1Ds) {
					return new OfDoubleArray1Ds(inputs -> inputs[0].getHeader(),
							inputs -> inputs[0].getTrace() + " " + name, arrayFunction.apply(duo), c);
				} else {
					throw new IllegalArgumentException(
							"Operator " + duo.toString() + " can only operate on columns of doubles or double arrays.");
				}

			});
		};
		return nameBuilder(name, function.apply(duo)).description("Unary operator " + name + ".").build();
	}

	private static Name getCollectorName(String name, DoubleCollector.Aggregation agg) {
		Function<DoubleCollector.Aggregation, RowsFunction<double[]>> f = a -> (range, inputs) -> {
			if (!(inputs[0] instanceof OfDoubleArray1Ds)) {
				throw new IllegalArgumentException(
						"Operator " + name.toString() + " can only operate on columns of double arrays.");
			}
			double[] output = new double[(int) range.size()];
			double d[][] = castColumn(inputs[0], OfDoubleArray1Ds.class).rows(range);
			for (int i = 0; i < output.length; i++) {
				output[i] = a.apply(d[i]);
			}
			return output;

		};
		Function<DoubleCollector.Aggregation, StackFunction> function = dc -> (p, s) -> {
			s.replaceAll(c -> new OfDoubles(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + " " + name, f.apply(dc), c));
		};
		return nameBuilder(name, function.apply(agg))
				.description("Aggregates row values in double array by " + name + ".").build();
	}

	private static String getId() {
		String id = null;
		do {
			id = UUID.randomUUID().toString().replaceAll("\\d", "").replaceAll("-", "");
		} while (id.length() < 8);
		return id.substring(0, 8);
	}

	private static String readInputStreamIntoString(InputStream inputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while (result != -1) {
			buf.write((byte) result);
			result = bis.read();
		}
		return buf.toString("UTF-8");
	}

	@Override
	protected List<Name> getNames() {
		return names;
	}

}
