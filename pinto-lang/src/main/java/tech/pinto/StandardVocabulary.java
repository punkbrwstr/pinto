package tech.pinto;

import static tech.pinto.Name.nameBuilder;
import static tech.pinto.Name.terminalNameBuilder;
import static tech.pinto.Column.*;

import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import tech.pinto.Pinto.Expression;
import tech.pinto.Pinto.StackFunction;
import tech.pinto.Window.Expanding;
import tech.pinto.Window.Rolling;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.Chart;
import tech.pinto.tools.ID;
import tech.pinto.Pinto.Stack;

public class StandardVocabulary extends Vocabulary {
    
    protected final List<Name.Builder> names;
	
	public StandardVocabulary() {
		names = new ArrayList<>(Arrays.asList(
			terminalNameBuilder("def", StandardVocabulary::def)
				.description("Defines the expression as the preceding name literal."),
			terminalNameBuilder("undef", StandardVocabulary::undef)
				.description("Defines the expression as the preceding name literal."),
			terminalNameBuilder("help", StandardVocabulary::help)
				.description("Prints help for the preceding name literal or all names if one has not been specified."),
			terminalNameBuilder("list", StandardVocabulary::list)
				.description("Shows description for all names."),
			terminalNameBuilder("eval", StandardVocabulary::eval)
				.description("Evaluates the expression over date range between two *date* columns over *periodicity*.")
				.indexer("[periodicity=B,date=today today,:]"),
			terminalNameBuilder("import", StandardVocabulary::imp)
				.description("Executes pinto expressions contained files specified by file names in string columns."),
			terminalNameBuilder("to_csv", StandardVocabulary::to_csv)
				.indexer("[filename,periodicity=B,date=today today,:]")
				.description("Evaluates the expression over the date range specified by *start, *end* and *freq* columns, exporting the resulting table to csv *filename*."),
			terminalNameBuilder("report", StandardVocabulary::report)
				.indexer("[HTML]")
				.description("Creates an HTML report from any columns labelled HTML."),
			terminalNameBuilder("to_file", StandardVocabulary::to_file)
				.indexer("[string, HTML]")
				.description("Creates an HTML report from any columns labelled HTML."),

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
				.description("Creates a constant date column with today's date."),
			nameBuilder("offset", StandardVocabulary::offset)
				.indexer("[date=today,periodicity=B,c=-1]")
				.description("Offset a *c* periods of *periodicity* from *date*."),
			nameBuilder("day_count", StandardVocabulary::dayCount)
				.description("Creates a column with count of days in each period."),
			nameBuilder("annualization_factor", StandardVocabulary::annualizationFactor)
				.indexer("[periodicity=\"range\"]")
				.description("Creates a column with annualization factor for the specified or evaluated range's periodicity."),
				
				
			nameBuilder("mkt", StandardVocabulary::mkt)
				.indexer("[tickers,fields]")
				.description("Adds columns for market data specified by *tickers* and *fields*."),
			nameBuilder("pi", StandardVocabulary::pi)
				.description("Creates a constant double column with the value pi."),
			nameBuilder("moon", StandardVocabulary::moon)
				.description("Creates a double column with values corresponding the phase of the moon."),
			nameBuilder("range", StandardVocabulary::range)
				.indexer("[c=1 4]")
				.description("Creates double columns corresponding to integers between first (inclusive) and second (exclusive) in *c*."),
			nameBuilder("read_csv", StandardVocabulary::readCsv)
				.indexer("[source,includes_header=\"true\"]")
				.description( "Reads CSV formatted table from file or URL specified as *source*."),
	

	/* data cleanup */
			nameBuilder("fill", StandardVocabulary::fill)
				.indexer("[periodicity=BQ-DEC,lookback=\"true\",default=NaN,:]")
				.description("Fills missing values with last good value, looking back one period of *freq* if *lookback* is true."),
			nameBuilder("join", StandardVocabulary::join)
				.indexer("[date,:]")
				.description("Joins columns over time, switching between columns on *dates*.\""),
			nameBuilder("upsample", StandardVocabulary::resample)
				.indexer("[periodicity=BM,:]")
				.description("Evaluates prior columns for lower frequency *periodicity*, carrying values forward for higher frequency evaluation periodicity."),
			nameBuilder("downsample", StandardVocabulary::downsample)
				.indexer("[periodicity=B,:]")
				.description("Evaluates prior columns for higher frequency *periodicity*, creating window columns."),
				
				
				
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
				
				
	/* windows */
			nameBuilder("rolling", StandardVocabulary::rolling_window)
					.description("Creates a rolling window of size *c* for each input.")
					.indexer("[c=2,:]"),
			nameBuilder("cross", StandardVocabulary::cross_window)
					.description("Creates a cross sectional window from input columns."),
			nameBuilder("expanding", StandardVocabulary::expanding_window)
					.description("Creates creates an expanding window starting on *start* or the start of the evaluated range.")
					.indexer("[date=\"range\",:]"),
			nameBuilder("rev_expanding", StandardVocabulary::rev_expanding_window)
					.description("Creates a reverse-expanding window containing values from the current period to the end of the range."),
			getStatisticName("sum", b -> new Statistic.Sum(b)),
			getStatisticName("mean", b -> new Statistic.Mean(b)),
			getStatisticName("zscore", b -> new Statistic.ZScore(b)),
			getStatisticName("std", b -> new Statistic.StandardDeviation(b)),
			getStatisticName("first", b-> Statistic.First),
			getStatisticName("last", b -> Statistic.Last),
			getStatisticName("change", b -> Statistic.Change),
			getStatisticName("pct_change", b -> Statistic.PercentChange),
			getStatisticName("min", b -> new Statistic.Min(b)),
			getStatisticName("max", b -> new Statistic.Max(b)),
			getStatisticName("median", b -> new Statistic.Median(b)),
			getStatisticName("product", b -> new Statistic.Product(b)),
			getPairStatisticName("covar", b -> new Statistic.PairCovariance(b)),
			getPairStatisticName("correl", b -> new Statistic.PairCorrelation(b)),
			nameBuilder("ewma", StandardVocabulary::ewma)
				.description("Exponentially weighted moving average calculated using *alpha* or defaulting to 2 / (N + 1)")
				.indexer("[alpha=\"none\",:]"),
			
	/* reporting */
			nameBuilder("grid", StandardVocabulary::grid)
				.indexer("[columns=3,HTML]")
				.description("Creates a grid layout in a report with all input columns labelled HTML as cells in the grid."),
			nameBuilder("table", StandardVocabulary::table)
				.indexer("[periodicity=B, date=-20 offset today,format=\"decimal\",row_header=\"date\",col_headers=\"true\",:]")
				.description("Creates a const string column with code for a table defined by input columns and with header HTML."),
			nameBuilder("chart", StandardVocabulary::chartSVG)
				.indexer("[title=\"\",date_format=\"\",number_format=\"#,##0.00\",width=750,height=350,background_color=\"#D0ECEC\",color=default_palette,data_labels=\"true\",periodicity=B,date=-20 offset today,:]")
				.description("Creates a const string column with code for an HTML line chart."),
			nameBuilder("bar", StandardVocabulary::barChartSVG)
				.indexer("[title=\"\",date_format=\"\",number_format=\"#,##0.00\",width=750,height=350,background_color=\"#D0ECEC\",color=default_palette,periodicity=B, date=-20 offset today,:]")
				.description("Creates a const string column with code for an HTML bar chart."),
			nameBuilder("histogram", StandardVocabulary::histogramChartSVG)
				.indexer("[title=\"\",date_format=\"\",number_format=\"#,##0.00\",width=750,height=350,background_color=\"#D0ECEC\",color=default_palette,periodicity=B,date=-20 offset today,:]")
				.description("Creates a const string column with code for an HTML histogram chart."),
			nameBuilder("rt", StandardVocabulary::rt)
				.indexer("[functions=\" BA-DEC offset expanding pct_change {YTD} today today eval\",format=\"percent\",digits=2,:]")
				.description("Creates a const string column with code for an HTML ranking table, applying each *function* to input columns and ranking the results."),
			nameBuilder("default_palette", StandardVocabulary::palette)
				.description("Creates const string columns with hex codes for default colors for charts."),
			nameBuilder("blue_palette", StandardVocabulary::bluePalette)
				.description("Creates const string columns with hex codes for blue colors for charts.")
		));

		for(Entry<String, Supplier<Periodicity<?>>> e : Periodicities.map.entrySet()) {
			names.add(nameBuilder(e.getKey(), periodicityConstantFunction.apply(e.getValue().get()))
						.description( "Creates a constant periodcities column for " + e.getKey()));
		}

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
    	names.add(getUnaryOperatorName("naToZero", x -> x != x ? 0 : x));
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

	private static void rolling_window(Pinto pinto, Stack s) {
		int size = (int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
		s.replaceAll(c -> {
			return new OfWindow(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + " rolling", getRollingWindowFunction(size), c);
		});
	}
	
	private static RowsFunctionGeneric<Window<?>> getRollingWindowFunction(int size) {
		return new RowsFunctionGeneric<Window<?>>(){
			@Override
			public <P extends Period<P>> Window<Rolling> getRows(PeriodicRange<P> range, Column<?>[] columns, Class<?> clazz) {
				P expandedWindowStart = range.periodicity().offset(-1 * (size - 1), range.start());
				PeriodicRange<P> expandedWindow = range.periodicity().range(expandedWindowStart, range.end());
				return new Window.Rolling(castColumn(columns[0], OfDoubles.class).rows(expandedWindow), size);
			}};
	}

	private static void cross_window(Pinto pinto, Stack s) {
		Column.OfDoubles[] a = s.toArray(new OfDoubles[] {});
		s.clear();
		s.add(new OfWindow(inputs -> "cross", inputs -> "cross", StandardVocabulary::crossWindowRowsFunction, a));
	}
	
	private static Window<?> crossWindowRowsFunction(PeriodicRange<?> range, Column<?>[] columns) {
		double[][] d = new double[columns.length][];
		for(int i = 0; i < d.length; i++) {
			d[i] = castColumn(columns[i], OfDoubles.class).rows(range);
		}
		return new Window.Cross(d);
	}

	private static void rev_expanding_window(Pinto pinto, Stack s) {
		s.replaceAll(c -> new OfWindow(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " rev_expanding", StandardVocabulary::revExpandingWindowRowsFunction, c));
	}
	
	private static Window<?> revExpandingWindowRowsFunction(PeriodicRange<?> range, Column<?>[] columns) {
		return new Window.ReverseExpanding(castColumn(columns[0], OfDoubles.class).rows(range));
	}

	private static void expanding_window(Pinto pinto, Stack s) {
		Optional<LocalDate> start = s.peekFirst() instanceof OfConstantStrings
				&& castColumn(s.removeFirst(), OfConstantStrings.class).getValue().equals("range") ? Optional.empty()
						: Optional.of(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		s.replaceAll(c -> {
			return new OfWindow(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + " expanding", getExpandingWindowFunction(start), c);
		});
	}
	
	private static RowsFunctionGeneric<Window<?>> getExpandingWindowFunction(Optional<LocalDate> start) {
		return new RowsFunctionGeneric<Window<?>>(){
			@Override
			public <P extends Period<P>> Window<Expanding> getRows(PeriodicRange<P> range, Column<?>[] columns, Class<?> clazz) {
				P expandedStart = start.isPresent() ? range.periodicity().from(start.get()) : range.start();
				int offset;
				double d[];
				if(expandedStart.isAfter(range.end())) {
					d = new double[] {};
					offset = (int) range.size() * -1;
				} else {
					PeriodicRange<P> expandedWindow = range.periodicity().range(expandedStart, range.end());
					offset = (int) range.periodicity().distance(expandedStart, range.start());
					d = castColumn(columns[0], OfDoubles.class).rows(expandedWindow);
				}
				return new Window.Expanding(d, offset);
			}};
	}

	private static Name.Builder getStatisticName(String name, Function<Boolean,Statistic> s) {
		Function<Boolean, RowsFunction<double[]>> f = b -> (range, inputs) -> {
			return s.apply(b).apply(castColumn(inputs[0], OfWindow.class).rows(range));
		};
		return nameBuilder(name, (Pinto pinto, Stack stack) ->  {
			boolean clearOnNan = Boolean.parseBoolean(castColumn(stack.removeFirst(),OfConstantStrings.class).getValue());
			stack.replaceAll(c -> new OfDoubles(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + " " + name, f.apply(clearOnNan), c));
		}).description("Calculates " + name + " for each view of window column inputs.")
				.indexer("[clear_on_nan=\"false\",:]");
	}

	private static Name.Builder getPairStatisticName(String name, Function<Boolean,Statistic.PairStatistic> s) {
		Function<Boolean, RowsFunction<double[]>> f = b -> (range, inputs) -> {
			return s.apply(b).apply(castColumn(inputs[0], OfWindow.class).rows(range),
					castColumn(inputs[1], OfWindow.class).rows(range));
		};
		return nameBuilder(name, (Pinto pinto, Stack stack) ->  {
			boolean clearOnNan = Boolean.parseBoolean(castColumn(stack.removeFirst(),OfConstantStrings.class).getValue());
			Stack temp = new Stack(stack);
			stack.clear();
			for(int i = 0; i < temp.size() - 1; i += 2) {
				stack.addLast(new OfDoubles(inputs -> inputs[0].getHeader() + inputs[1].getHeader(),
					inputs -> "(" + inputs[0].getTrace() + ") (" + inputs[1].getTrace() + ") " + name,
					f.apply(clearOnNan), temp.get(i), temp.get(i+1)));
			}
		}).description("Calculates " + name + " for each view from a pair of window column inputs.")
				.indexer("[clear_on_nan=\"false\",:]");
	}

	private static void ewma(Pinto pinto, Stack stack) {
		Column<?> alphaCol = stack.removeFirst();
		Optional<Double> alpha = alphaCol instanceof OfConstantDoubles ? 
				Optional.of(castColumn(alphaCol, OfConstantDoubles.class).getValue()) : Optional.empty();
		stack.replaceAll(c -> new OfDoubles(inputs -> inputs[0].getHeader(),
			inputs -> inputs[0].getTrace() + " ewma", (range, inputs) -> {
				return new tech.pinto.Statistic.EWMA(alpha).apply(castColumn(inputs[0],OfWindow.class).rows(range));
			}, c));
	}
	

	private static final Table def(Pinto pinto, Expression expression) {
		return new Table("Defined " + pinto.getNamespace().define(pinto, expression));
	}

	private static Table undef(Pinto pinto, Expression e) {
		String name = e.getNameLiteral() .orElseThrow(() -> new PintoSyntaxException("del requires a name literal."));
		pinto.getNamespace().undefine(name);
		return new Table("Undefined " + name);
	}

	private static Table help(Pinto pinto, Expression e) {
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		if (e.getNameLiteral().isPresent()) {
			String n = e.getNameLiteral().get();
			sb.append(pinto.getNamespace().getName(n).getHelp(n)).append(crlf);
		} else {
			sb.append("Pinto help").append(crlf);
			sb.append("Built-in names:").append(crlf);
			List<String> l = new ArrayList<>(pinto.getNamespace().getNames());
			List<String> l2 = new ArrayList<>();
			for (int i = 0; i < l.size(); i++) {
				if (pinto.getNamespace().getName(l.get(i)).isBuiltIn()) {
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
		return new Table(sb.toString());
	}

	private static Table list(Pinto pinto, Expression e) {
		StringBuilder sb = new StringBuilder();
		String crlf = System.getProperty("line.separator");
		pinto.getNamespace().getNames().stream().map(s -> pinto.getNamespace().getName(s)).forEach(name -> {
			sb.append(name.toString()).append("|").append(name.getIndexString()).append("|")
					.append(name.getDescription()).append(crlf);
		});
		return new Table(sb.toString());
	}

	private static Table eval(Pinto pinto, Expression e) {
		Table t = new Table();
		e.accept(pinto, t);
		Stack s = t.flatten();
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		LinkedList<LocalDate> dates = new LinkedList<>();
		while ((!s.isEmpty()) && dates.size() < 2 && s.peekFirst().getHeader().equals("date")) {
			dates.add(((Column.OfConstantDates) s.removeFirst()).getValue());
		}
		t.evaluate(periodicity.range(dates.removeLast(), dates.isEmpty() ? LocalDate.now() : dates.peek()));
		return t;
	}

	private static Table imp(Pinto pinto, Expression expression) {
		Table t = new Table();
		expression.accept(pinto, t);
		for (Stack s : t.takeTop()) {
			while (!s.isEmpty()) {
				String filename = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
				int lineNumber = 0;
				try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
					String line = null;
					Pinto.Expression e = new Pinto.Expression(false);
					while ((line = reader.readLine()) != null) {
						lineNumber++;
						List<Pinto.Expression> expressions =  pinto.parse(line, e);
						Expression next = expressions.size() > 0 
								&& !expressions.get(expressions.size()-1).hasTerminal() ?
										expressions.remove(expressions.size()-1) : new Expression(false);
						for(int i = 0; i < expressions.size(); i++) {
							expressions.get(i).evaluate(pinto);
						}
						e = next;
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
		return new Table("Successfully executed");
	}

	private static Table to_file(Pinto pinto, Expression e) {
		Table t = new Table();
		e.accept(pinto, t);
		Stack s = t.flatten();
		String filename = castColumn(s.removeFirst(),OfConstantStrings.class).getValue();
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
			while(!s.isEmpty()) {
				out.print(castColumn(s.removeFirst(),OfConstantStrings.class).getValue());
			}
		} catch (IOException err) {
			throw new IllegalArgumentException("Unable to open file \"" + filename + "\" for export");
		}
		return new Table("Successfully exported " + filename);
	}

	private static Table to_csv(Pinto pinto, Expression e) {
		Table t = new Table();
		e.accept(pinto, t);
		Stack s = t.flatten();
		String filename = castColumn(s.removeFirst(),OfConstantStrings.class).getValue();
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		LinkedList<LocalDate> dates = new LinkedList<>();
		while ((!s.isEmpty()) && dates.size() < 2 && s.peekFirst().getHeader().equals("date")) {
			dates.add(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		}
		t.evaluate(periodicity.range(dates.removeLast(), dates.isEmpty() ? LocalDate.now() : dates.peek()));
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
			out.print(t.toCsv());
		} catch (IOException err) {
			throw new IllegalArgumentException("Unable to open file \"" + filename + "\" for export");
		}
		return new Table("Successfully exported");
	}

	private static Table report(Pinto pinto, Expression e) {
		String id = ID.getId();
		e.setNameLiteral("~report-" + id);
		pinto.getNamespace().define(pinto, e);
		try {
			Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + pinto.getPort() + "/pinto/report?p=" + id));
		} catch (Exception err) {
			throw new PintoSyntaxException("Unable to open report", err);
		}
		return new Table("Report id: " + id);
	}

	private static void only(Pinto pinto, Table t) {
		List<Stack> indexed = t.takeTop();
		t.clearTop();
		t.insertAtTop(indexed);
	}

	private static void clear(Pinto pinto, Stack s) {
		s.clear();
	}

	private static void rev(Pinto pinto, Stack s) {
		Collections.reverse(s);
	}

	private static void pull(Pinto pinto, Stack s) {
		;
	}

	private static void copy(Pinto pinto, Stack s) {
		int times = (int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
		Stack temp = new Stack();
		s.stream().forEach(temp::addFirst);
		for (int j = 0; j < times - 1; j++) {
			temp.stream().map(Column::clone).forEach(s::addFirst);
		}
	}

	private static void roll(Pinto pinto, Stack s) {
		int times = (int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
		for (int j = 0; j < times; j++) {
			s.addLast(s.removeFirst());
		}
	}

	private static void columns(Pinto pinto, Stack s) {
		s.addFirst(new OfConstantDoubles(s.size()));
	}

	private static void index(Pinto pinto, Table t) {
		Stack s = t.takeTop().get(0);
		List<String> endpoints = new LinkedList<>();
		while (!s.isEmpty() && endpoints.size() < 2 && s.peekFirst().getHeader().equals("c")) {
			endpoints
					.add(Integer.toString((int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue()));
		}
		t.insertAtTop(s);
		String index = endpoints.size() == 1 ? ":" + endpoints.get(0) : endpoints.get(1) + ":" + endpoints.get(0);
		new Indexer(pinto, new HashSet<>(),index).accept(pinto, t);
	}

	private static void today(Pinto p, Stack s) {
		s.addFirst(new OfConstantDates(() -> LocalDate.now()));
	}

	private static void offset(Pinto pinto, Stack s) {
		LocalDate date = castColumn(s.removeFirst(), OfConstantDates.class).getValue();
		Periodicity<?> periodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		int count = castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		s.addFirst(new OfConstantDates(periodicity.offset(count, date)));
	}

	private static void dayCount(Pinto pinto, Stack s) {
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

	private static void annualizationFactor(Pinto pinto, Stack s) {
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
	
	private static void mkt(Pinto pinto, Stack s) {
		String tickers = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
		String fields = castColumn(s.removeFirst(), OfConstantStrings.class).getValue();
		pinto.marketdata.getStackFunction(tickers.concat(":").concat(fields)).accept(pinto,s);
	}

	private static void pi(Pinto pinto, Stack s) {
		s.addFirst(new OfConstantDoubles(Math.PI));
	}

	private static void moon(Pinto pinto, Stack s) {
		s.addFirst(new OfDoubles(i -> "moon", StandardVocabulary::moonRowFunction));
	}

	private static double[] moonRowFunction(PeriodicRange<?> range, Column<?>[] inputs) {
		return range.dates().stream().mapToDouble(d -> {
			return new tech.pinto.tools.MoonPhase(d).getPhase();
		}).toArray();
	}

	private static void range(Pinto pinto, Stack s) {
		LinkedList<Integer> endpoints = new LinkedList<Integer>();
		while (!s.isEmpty() && endpoints.size() < 2 && s.peekFirst().getHeader().equals("c")) {
			endpoints.addLast((int) castColumn(s.removeFirst(), OfConstantDoubles.class).getValue().doubleValue());
		}
		int start = endpoints.size() == 2 ? endpoints.removeLast() : 1;
		int end = endpoints.removeFirst();
		IntStream.range(start, end).mapToDouble(i -> (double) i).mapToObj(value -> new OfConstantDoubles(value))
				.forEach(s::addFirst);
	}

	private static void readCsv(Pinto pinto, Stack s) {
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

	private static void fill(Pinto pinto, Stack s) {
		Periodicity<?> p = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		boolean lb = Boolean.parseBoolean(castColumn(s.removeFirst(), OfConstantStrings.class).getValue());
		double defaultValue = castColumn(s.removeFirst(), OfConstantDoubles.class).getValue();
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
					double[] d = castColumn(inputs[0], OfDoubles.class).rows(r);
					int i = skip;
					while (i > 0 && d[i] != d[i]) {
						i--;
					}
					d[skip] = d[i];
					if(d[skip] != d[skip]) {
						d[skip] = defaultValue;
					}
					for (i = skip + 1; i < d.length; i++) {
						double inputValue = d[i];
						if(inputValue != inputValue) {
							d[i] = d[i - 1];
						}
					}
					return Arrays.copyOfRange(d, skip, d.length);
				}
			};
		};
		s.replaceAll(oldColumn -> {
			return new OfDoubles(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " fill",
					fillRowFunction.apply(p).apply(lb), oldColumn);
		});
	}

	private static void join(Pinto pinto, Stack s) {
		LinkedList<LocalDate> cutoverDates = new LinkedList<>();
		while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("date")) {
			cutoverDates.add(castColumn(s.removeFirst(), OfConstantDates.class).getValue());
		}
		OfDoubles[] inputStack = s.toArray(new OfDoubles[] {});
		if(inputStack.length == 0) {
			throw new PintoSyntaxException("No columns to join.");
		}
		s.clear();
		Function<LinkedList<LocalDate>, RowsFunctionGeneric<double[]>> joinRowFunction = cd -> {
			return new RowsFunctionGeneric<double[]>() {
				@Override
				public <P extends Period<P>> double[] getRows(PeriodicRange<P> range, Column<?>[] inputArray,
						Class<?> clazz) {
					double[] output = new double[(int) range.size()];
					int outputIndex = 0;
					Stack inputs = new Stack(Arrays.asList(inputArray));
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

	private static void resample(Pinto pinto, Stack s) {
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

	private static void downsample(Pinto pinto, Stack s) {
		Periodicity<?> newPeriodicity = castColumn(s.removeFirst(), OfConstantPeriodicities.class).getValue();
		s.replaceAll(c -> {
			return new OfWindow(inputs -> inputs[0].getHeader(),
					inputs -> inputs[0].getTrace() + newPeriodicity.code() + " resample",
					getDownsampleFunction(newPeriodicity), c);
		});
	}

	private static <N extends Period<N>> RowsFunctionGeneric<Window<?>> getDownsampleFunction(Periodicity<N> p) {
		return new RowsFunctionGeneric<Window<?>>() {
			@Override
			public <P extends Period<P>> Window<?> getRows(PeriodicRange<P> range, Column<?>[] inputs, Class<?> clazz) {
				N newStart = p.roundUp(range.start().startDate());
				N newEnd = p.from(range.end().endDate());
				PeriodicRange<N> newDr = p.range(newStart, newEnd);
				double[] d = (double[]) inputs[0].rows(newDr);
				return new Window.Downsample(d, range.dates(), newDr);
			}
		};
	}

	private static void hcopy(Pinto pinto, Stack stack) {
		List<String> headers = stack.stream().map(Column::getHeader).collect(Collectors.toList());
		Collections.reverse(headers);
		for(int i = 0; i < headers.size(); i++) {
			stack.addFirst(new OfConstantStrings(headers.get(i)));
		}
	}

	private static void hpaste(Pinto pinto, Stack stack) {
		List<String> headers = new ArrayList<>();
		while(stack.peekFirst() instanceof OfConstantStrings && stack.peekFirst().getHeader().equals("string")) {
			headers.add(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		}
		boolean repeat = Boolean.parseBoolean(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		for(int i = 0; i < stack.size() && (i < headers.size() || repeat); i++) {
			stack.get(i).setHeader(headers.get(i % headers.size()));
		}
	}

	private static void hformat(Pinto pinto, Stack s) {
		MessageFormat format = new MessageFormat(
				castColumn(s.removeFirst(), OfConstantStrings.class).getValue().replaceAll("\\{\\}", "\\{0\\}"));
		s.replaceAll(c -> {
			String header = format.format(new Object[] { c.getHeader() });
			c.setHeader(header);
			return c;
		});
	}

	private static void cat(Pinto pinto, Stack s) {
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


	private static void palette(Pinto pinto, Stack stack) {
		stack.addLast(new OfConstantStrings("#92d050","color"));
		stack.addLast(new OfConstantStrings("#c5be97","color"));
		stack.addLast(new OfConstantStrings("#8db4e3","color"));
		stack.addLast(new OfConstantStrings("#ffff99","color"));
		stack.addLast(new OfConstantStrings("#fddf70","color"));
		stack.addLast(new OfConstantStrings("#ff7c80","color"));
		stack.addLast(new OfConstantStrings("#f09ebf","color"));
		stack.addLast(new OfConstantStrings("#efff59","color"));
		stack.addLast(new OfConstantStrings("#43d2ff","color"));
		stack.addLast(new OfConstantStrings("#888888","color"));
		stack.addLast(new OfConstantStrings("#00b050","color"));
		stack.addLast(new OfConstantStrings("#846802","color"));
		stack.addLast(new OfConstantStrings("#0070c0","color"));
		stack.addLast(new OfConstantStrings("#ffff00","color"));
		stack.addLast(new OfConstantStrings("#fbc913","color"));
		stack.addLast(new OfConstantStrings("#ea5816","color"));
		stack.addLast(new OfConstantStrings("#d42069","color"));
		stack.addLast(new OfConstantStrings("#9fb000","color"));
		stack.addLast(new OfConstantStrings("#007194","color"));
		stack.addLast(new OfConstantStrings("#7f7f7f","color"));
		stack.addLast(new OfConstantStrings("#ccffcc","color"));
		stack.addLast(new OfConstantStrings("#ccffff","color"));
		stack.addLast(new OfConstantStrings("#ffffcc","color"));
		stack.addLast(new OfConstantStrings("#ffcccc","color"));
	}
	
	private static void bluePalette(Pinto pinto, Stack stack) {
		stack.addLast(new OfConstantStrings("#12426d","color"));
		stack.addLast(new OfConstantStrings("#2383db","color"));
		stack.addLast(new OfConstantStrings("#85baeb","color"));
		stack.addLast(new OfConstantStrings("#c3ddf5","color"));
		stack.addLast(new OfConstantStrings("#aadaac","color"));
		stack.addLast(new OfConstantStrings("#489452","color"));
		stack.addLast(new OfConstantStrings("#306236","color"));
		stack.addLast(new OfConstantStrings("#04400b","color"));
		stack.addLast(new OfConstantStrings("#282828","color"));
		stack.addLast(new OfConstantStrings("#515151","color"));
		stack.addLast(new OfConstantStrings("#808080","color"));
		stack.addLast(new OfConstantStrings("#cdcdcd","color"));
		stack.addLast(new OfConstantStrings("#adb0d6","color"));
		stack.addLast(new OfConstantStrings("#767cbb","color"));
		stack.addLast(new OfConstantStrings("#5556aa","color"));
		stack.addLast(new OfConstantStrings("#3b3c8d","color"));
	}
	
	private static void chartSVG(Pinto pinto, Stack stack) {
		String title = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		String dateFormat = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		String numberFormat = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		int width = castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		int height = castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		Color background = Color.decode(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		List<Color> colors = new ArrayList<>();
		while(stack.peekFirst().getHeader().equals("color")) {
			colors.add(Color.decode(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue()));
		}
		boolean dataLabels = Boolean.parseBoolean(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		Pinto.Expression e = new Pinto.Expression(false);
		final Stack s3 = new Stack(stack);
		e.addFunction(Pinto.toTableConsumer((p, s) -> s.addAll(s3)));
		e.setTerminal(StandardVocabulary::eval);
		stack.clear();
		stack.add(new OfConstantStrings(() -> Chart.lineChart(e.evaluate(pinto),"chart-" + ID.getId(),
							title, dateFormat, numberFormat, width, height, colors, background, dataLabels), "HTML"));
	}

	private static void barChartSVG(Pinto pinto, Stack stack) {
		String title = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		String dateFormat = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		String numberFormat = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		int width = castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		int height = castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		Color background = Color.decode(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		List<Color> colors = new ArrayList<>();
		while(stack.peekFirst().getHeader().equals("color")) {
			colors.add(Color.decode(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue()));
		}
		Pinto.Expression e = new Pinto.Expression(false);
		final Stack s3 = new Stack(stack);
		e.addFunction(Pinto.toTableConsumer((p, s) -> s.addAll(s3)));
		e.setTerminal(StandardVocabulary::eval);
		stack.clear();
		stack.add(new OfConstantStrings(() -> Chart.barChart(e.evaluate(pinto),"chart-" + ID.getId(),
							title, dateFormat, numberFormat, width, height, colors, background), "HTML"));
	}

	private static void histogramChartSVG(Pinto pinto, Stack stack) {
		String title = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		String dateFormat = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		String numberFormat = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		int width = castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		int height = castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		Color background = Color.decode(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		List<Color> colors = new ArrayList<>();
		while(stack.peekFirst().getHeader().equals("color")) {
			colors.add(Color.decode(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue()));
		}
		Pinto.Expression e = new Pinto.Expression(false);
		final Stack s3 = new Stack(stack);
		e.addFunction(Pinto.toTableConsumer((p, s) -> s.addAll(s3)));
		e.setTerminal(StandardVocabulary::eval);
		stack.clear();
		stack.add(new OfConstantStrings(() -> Chart.histogramChart(e.evaluate(pinto),"chart-" + ID.getId(),
							title, dateFormat, numberFormat, width, height, colors, background), "HTML"));
	}

	private static void grid(Pinto pinto, Stack stack) {
		int columns = Double.valueOf(castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue()).intValue();
		Stack s = new Stack(stack);
		stack.clear();
		stack.add(new OfConstantStrings(() -> {
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
			return sb.toString();
		}, "HTML") );
	}

	private static void table(Pinto pinto, Stack stack) {
		Periodicity<?> periodicity = castColumn(stack.removeFirst(), OfConstantPeriodicities.class).getValue();
		LinkedList<LocalDate> d = new LinkedList<>();
		while ((!stack.isEmpty()) && d.size() < 2 && stack.peekFirst().getHeader().equals("date")) {
			d.add(castColumn(stack.removeFirst(), OfConstantDates.class).getValue());
		}
		PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek());
		NumberFormat nf = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue().equals("percent")
				? NumberFormat.getPercentInstance()
				: NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(4);
		nf.setGroupingUsed(false);
		String rowHeader = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue();
		boolean colHeaders = Boolean.parseBoolean(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		Stack s = new Stack(stack);
		stack.clear();
		stack.add(new OfConstantStrings(() -> {
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
			return sb.toString();
		}, "HTML"));
	}

	private static void rt(Pinto pinto, Stack stack) {
		LinkedList<String> functions = new LinkedList<>();
		while ((!stack.isEmpty()) && stack.peekFirst().getHeader().equals("functions")) {
			functions.addFirst(castColumn(stack.removeFirst(), OfConstantStrings.class).getValue());
		}
		NumberFormat nf = castColumn(stack.removeFirst(), OfConstantStrings.class).getValue().equals("percent")
				? NumberFormat.getPercentInstance()
				: NumberFormat.getNumberInstance();
		int digits = castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().intValue();
		nf.setMaximumFractionDigits(digits);
		int columns = functions.size();
		int rows = stack.size();
		final Stack s = new Stack(stack);
		stack.clear();
		stack.add(new OfConstantStrings(() -> {
			String[] labels = new String[rows];
			String[] headers = new String[columns];
			String[][] cells = new String[rows][columns];
			for (int i = 0; i < columns; i++) {
				double[][] values = new double[s.size()][2];
				for (int j = 0; j < s.size(); j++) {
					Pinto.Expression expression = new Pinto.Expression(false);
					final int J = j;
					expression.addFunction(Pinto.toTableConsumer((p, s2) -> {
						s2.add(s.get(J).clone());
					}));
					double[][] d;
					Table t;
					try {
						pinto.parse(functions.get(i), expression);
						t = expression.evaluate(pinto);
						d = t.toColumnMajorArray();
					} catch(RuntimeException e) {
						throw new RuntimeException("Error in rt function \"" + expression.getText() + "\".", e);
					}
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
			return sb.toString();
		}, "HTML") );
	}

	private static Function<Periodicity<?>, StackFunction> periodicityConstantFunction = per -> (p, s) -> s
			.addFirst(new Column.OfConstantPeriodicities(per));

	private static Name.Builder getBinaryOperatorName(String name, DoubleBinaryOperator dbo) {
		Function<DoubleBinaryOperator, RowsFunction<double[]>> doubledouble = o -> (range, inputs) -> {
			double[] l = castColumn(inputs[1], Column.OfDoubles.class).rows(range);
			double[] r = castColumn(inputs[0], Column.OfDoubles.class).rows(range);
			for (int j = 0; j < l.length; j++) {
				l[j] = o.applyAsDouble(l[j], r[j]);
			}
			return l;
		};
		Function<DoubleBinaryOperator, RowsFunction<Window<?>>> arrayarray = o -> (range, inputs) -> {
			Window<?> l = castColumn(inputs[1], OfWindow.class).rows(range);
			Window<?> r = castColumn(inputs[0], OfWindow.class).rows(range);
			return l.apply(o, r);
		};
		Function<DoubleBinaryOperator, StackFunction> function = dc -> (p, stack) -> {
			int rightCount = (int) castColumn(stack.removeFirst(), OfConstantDoubles.class).getValue().doubleValue();
			if (stack.size() < rightCount + 1) {
				throw new IllegalArgumentException("Not enough inputs for " + name);
			}
			Stack rights = new Stack(stack.subList(0, rightCount));
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
							inputs -> "((" + inputs[1].getTrace() + ") (" + inputs[0].getTrace() + ") " + name + ")",
							doubledouble.apply(dc), right, lefts.get(i)));
				} else if (right instanceof OfWindow
						&& lefts.get(i) instanceof OfWindow) {
					stack.add(new OfWindow(inputs -> inputs[1].getHeader(),
							inputs -> "((" + inputs[1].getTrace() + ") (" + inputs[0].getTrace() + ") " + name + ")",
							arrayarray.apply(dc), right, lefts.get(i)));
				} else {
					throw new IllegalArgumentException(
							"Operator " + name + " can only operate on columns of doubles or double arrays.");
				}
			}

		};
		return nameBuilder(name, function.apply(dbo))
				.description("Binary operator " + name
						+ " that operates on *width* columns at a time with fixed right-side operand.")
				.indexer("[width=1,:]");
	}

	private static Name.Builder getUnaryOperatorName(String name, DoubleUnaryOperator duo) {
		Function<DoubleUnaryOperator, RowsFunction<double[]>> doubleFunction = o -> (range, inputs) -> {
			double d[] = castColumn(inputs[0], OfDoubles.class).rows(range);
			for (int i = 0; i < d.length; i++) {
				d[i] = o.applyAsDouble(d[i]);
			}
			return d;
		};
		Function<DoubleUnaryOperator, RowsFunction<Window<?>>> arrayFunction = o -> (range, inputs) -> {
			Window<?> w = castColumn(inputs[0], OfWindow.class).rows(range);
			return w.apply(o);
		};
		Function<DoubleUnaryOperator, StackFunction> function = dc -> (p, s) -> {
			s.replaceAll(c -> {
				if (c instanceof OfDoubles) {
					return new OfDoubles(inputs -> inputs[0].getHeader(),
							inputs -> inputs[0].getTrace() + " " + name, doubleFunction.apply(duo), c);
				} else if (c instanceof OfWindow) {
					return new OfWindow(inputs -> inputs[0].getHeader(),
							inputs -> inputs[0].getTrace() + " " + name, arrayFunction.apply(duo), c);
				} else {
					throw new IllegalArgumentException(
							"Operator " + name + " can only operate on columns of doubles, not " + c.getClass());
				}

			});
		};
		return nameBuilder(name, function.apply(duo)).description("Unary operator " + name + ".");
	}


	@Override
	protected List<Name> getNames() {
		return names.stream().map(b -> b.build()).collect(Collectors.toList());
	}

}
