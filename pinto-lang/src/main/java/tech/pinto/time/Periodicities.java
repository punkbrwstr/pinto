package tech.pinto.time;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

public class Periodicities {
	
	public static final Map<String,Supplier<Periodicity<?>>> map = 
			new ImmutableMap.Builder<String,Supplier<Periodicity<?>>>()
			.put("B",() -> new BusinessDaily())
			.put("W-FRI",() -> new FridayWeekly())
			.put("BM",() -> new BusinessMonthly())
			.put("BQ-DEC",() -> new BusinessQuarterly())
			.put("BA-DEC",() -> new BusinessYearly())
			.build();
	
	@SuppressWarnings("unchecked")
	public static <P extends Period> Periodicity<P> get(String code) {
		if(!map.containsKey(code)) {
			throw new IllegalArgumentException("Periodicity code \"" + code + "\" not found.");
		}
		return (Periodicity<P>) map.get(code).get();
	}
	
	public static Set<String> allCodes() {
		return map.keySet();
	}

}
