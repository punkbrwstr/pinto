package tech.pinto;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;

public class IEXMarketData implements MarketData {
	
	private enum Field {
		OPEN,HIGH,LOW,CLOSE,VOLUME,VWAP;
		public static final Field[] values = values();
	}
	
	private static Map<String,TreeMap<LocalDate,double[]>> cache = new HashMap<>();

	@Override
	public <P extends Period> Function<PeriodicRange<?>, double[][]> getFunction(List<String> securities,
			List<String> fields) {
		return range -> {
			double[][] d = new double[securities.size() * fields.size()][(int) range.size()];
			for(int i = 0; i < d.length; i++) {
				Arrays.fill(d[i], Double.NaN);
			}
			for(int i = 0; i < securities.size(); i++) {
				String ticker = securities.get(i).toLowerCase();
				if((!cache.containsKey(ticker))
						|| cache.get(ticker).lastKey().isBefore(Periodicities.get("B").offset(-1, LocalDate.now()))) {
					loadCache(ticker);
				}
				List<LocalDate> dates = range.dates();
				SortedMap<LocalDate,double[]> tree = cache.get(ticker);
				for(int j = 0; j < dates.size(); j++) {
					tree = tree.tailMap(dates.get(j));
					if(tree.firstKey().equals(dates.get(j))) {
						double[] prices = tree.get(tree.firstKey());
						for(int k = 0; k < fields.size(); k++) {
							Field f = null;
							try {
								f = Field.valueOf(fields.get(k).toUpperCase());
							} catch(IllegalArgumentException e) {
								throw new IllegalArgumentException("Field \"" + fields.get(k) + "\" not supported by IEX.");
							}
							d[i * fields.size() + k][j] = prices[f.ordinal()];
						}
					}
				}
			}
			return d;
		};
	}
	
	private void loadCache(String ticker) {
		String url = MessageFormat.format("https://api.iextrading.com/1.0/stock/{0}/chart/5y",ticker);
		Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
		Gson gson = new Gson();
		try (Reader reader = new InputStreamReader(new URL(url).openStream(), "UTF-8")) {
			List<Map<String, Object>> l = gson.fromJson(reader, type);
			TreeMap<LocalDate,double[]> t = new TreeMap<>();
			for(Map<String, Object> m : l) {
				double[] d = new double[Field.values.length];
				for(int i = 0; i < Field.values.length; i++) {
					d[i] = (Double) m.get(Field.values[i].name().toLowerCase());
				}
				t.put(LocalDate.parse((String)m.get("date")), d);
			}
			cache.put(ticker, t);
		} catch (Exception e) {
			throw new RuntimeException("Unable to load IEX data for ticker: \"" + ticker + "\"", e);
		}
		
	}
	
	public static void main(String[] args) {
	}

}
