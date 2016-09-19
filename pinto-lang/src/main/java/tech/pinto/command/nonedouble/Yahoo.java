package tech.pinto.command.nonedouble;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

import tech.pinto.Cache;
import tech.pinto.data.DoubleData;
import tech.pinto.time.BusinessDaily;
import tech.pinto.time.BusinessMonthly;
import tech.pinto.time.FridayWeekly;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicity;

public class Yahoo extends CachedDoubleCommand {
	
	static private final Map<Periodicity<?>,String> FREQ = 
			new ImmutableMap.Builder<Periodicity<?>,String>()
				.put(new BusinessDaily(),"d")
				.put(new FridayWeekly(),"w")
				.put(new BusinessMonthly(),"m")
				.build();
	private final ArrayDeque<String> tickers = new ArrayDeque<>();
	
	public Yahoo(Cache cache, String... args) {
		super("yhoo", cache, args);
		Stream.of(args).forEach(tickers::addLast);
		inputCount = 0;
		outputCount = tickers.size();
	}

	@Override
	public <P extends Period> DoubleData evaluate(PeriodicRange<P> range) {
		if(!FREQ.containsKey(range.periodicity())) {
			throw new IllegalArgumentException("Unsupported periodicity for yahoo finance data.");
		}
		String ticker = tickers.removeFirst();
		LocalDate start = range.start().endDate();
		LocalDate end = range.end().endDate();
		String url = MessageFormat.format("http://chart.finance.yahoo.com/table.csv?" +
				"s={0}&a={1}&b={2}&c={3,number,####}&d={4}&e={5}&f={6,number,####}&g={7}&ignore=.csv",
				ticker,
				start.getMonthValue()-1,start.getDayOfMonth(),start.getYear(),
				end.getMonthValue()-1,end.getDayOfMonth(),end.getYear(),
				FREQ.get(range.periodicity())
				);
		//System.out.println("yahoo: " + url);
		Map<P,Double> quotes = new HashMap<>();
		Periodicity<P> p = range.periodicity();
		try {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"))) {
				reader.readLine(); // burn one
		    	for (String line; (line = reader.readLine()) != null;) {
		        	String[] cols = line.split(",");
		        	quotes.put(p.from(LocalDate.parse(cols[0])), Double.valueOf(cols[4]));
		    	}
			}
		} catch(Exception e) {
			throw new IllegalArgumentException("Problem reading yahoo finance data: " + e.getMessage(), e);
		}
		
		DoubleStream.Builder b = DoubleStream.builder();
		for(P per : range.values()) {
			if(quotes.containsKey(per)) {
				b.accept(quotes.get(per));
			} else {
				b.accept(Double.NaN);
			}
		}
		return new DoubleData(range, ticker,b.build());
	}
	

}
