package tech.pinto;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class IEXMarketData implements MarketData {

	@Override
	public <P extends Period> Function<PeriodicRange<?>, double[][]> getFunction(List<String> securities,
			List<String> fields) {
		// TODO Auto-generated method stub
		return MarketData.super.getFunction(securities, fields);
	}
	
	public static void main(String[] args) {
		String url = MessageFormat.format("https://api.iextrading.com/1.0/stock/{0}/chart/5y","goog");
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		Gson gson = new Gson();
		try (Reader reader = new InputStreamReader(new URL(url).openStream(), "UTF-8")) {
			Map<String, String> myMap = gson.fromJson(reader, type);
			System.out.println("asdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
