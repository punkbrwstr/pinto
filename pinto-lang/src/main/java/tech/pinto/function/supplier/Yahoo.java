package tech.pinto.function.supplier;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

import tech.pinto.Cache;
import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.time.BusinessDaily;
import tech.pinto.time.BusinessMonthly;
import tech.pinto.time.FridayWeekly;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicity;

public class Yahoo extends CachedSupplierFunction {

	static private final Map<Periodicity<?>, String> FREQ = new ImmutableMap.Builder<Periodicity<?>, String>()
			.put(new BusinessDaily(), "d").put(new FridayWeekly(), "w").put(new BusinessMonthly(), "m").build();
	private final ArrayDeque<String> tickers = new ArrayDeque<>();

	public Yahoo(Cache cache, LinkedList<Function> inputs, String... args) {
		super("yhoo", cache, inputs, args);
		Stream.of(args).forEach(tickers::addLast);
	}

	@Override
	public <P extends Period> List<TimeSeries> evaluateAllUncached(PeriodicRange<P> range) {
		if (!FREQ.containsKey(range.periodicity())) {
			throw new IllegalArgumentException("Unsupported periodicity for yahoo finance data.");
		}
		List<TimeSeries> output = new ArrayList<>();
		for (String ticker : tickers) {
			LocalDate start = range.start().endDate();
			LocalDate end = range.end().endDate();
			String url = MessageFormat.format(
					"http://chart.finance.yahoo.com/table.csv?"
							+ "s={0}&a={1}&b={2}&c={3,number,####}&d={4}&e={5}&f={6,number,####}&g={7}&ignore=.csv",
					ticker, start.getMonthValue() - 1, start.getDayOfMonth(), start.getYear(), end.getMonthValue() - 1,
					end.getDayOfMonth(), end.getYear(), FREQ.get(range.periodicity()));
			System.out.println("yahoo: " + url);
			Map<P, Double> quotes = new HashMap<>();
			Periodicity<P> p = range.periodicity();
			try {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(new URL(url).openStream(), "UTF-8"))) {
					reader.readLine(); // burn one
					for (String line; (line = reader.readLine()) != null;) {
						String[] cols = line.split(",");
						quotes.put(p.from(LocalDate.parse(cols[0])), Double.valueOf(cols[4]));
					}
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Problem reading yahoo finance data: " + e.getMessage(), e);
			}

			DoubleStream.Builder b = DoubleStream.builder();
			for (P per : range.values()) {
				if (quotes.containsKey(per)) {
					b.accept(quotes.get(per));
				} else {
					b.accept(Double.NaN);
				}
			}
			output.add(new TimeSeries(range, ticker, b.build()));
		}
		return output;
	}
	
	public static Supplier<FunctionHelp> getHelp() {
		return () -> new FunctionHelp.Builder("yhoo")
				.outputs("n + z")
				.description("Retrieves online price history for each *ticker*.")
				.parameter("ticker<sub>1</sub>")
				.parameter("ticker<sub>z</sub>")
				.build();
	}

	@Override
	protected int myOutputCount() {
		return tickers.size();
	}

}
