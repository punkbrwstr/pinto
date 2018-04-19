package tech.pinto;

import java.util.List;
import java.util.function.Function;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public interface MarketData {

	default public <P extends Period> Function<PeriodicRange<?>, double[][]> getFunction(List<String> securities, List<String> fields ) {
			return range -> new double[securities.size() * fields.size()][(int) range.size()];
	}
					

}
