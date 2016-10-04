package tech.pinto.tests;

import org.junit.BeforeClass;

import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.Pinto.Response;
import tech.pinto.PintoSyntaxException;
import tech.pinto.TimeSeries;
import tech.pinto.time.PeriodicRange;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StatementTester {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	private final String EVAL = " eval(2016-01-01,2016-01-01)";
	private static Pinto pinto;

	@BeforeClass
	public static void setup() {
		pinto = AllTests.component.pinto();
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testSave() throws Exception {
		pinto.execute("1.1 def(thing)");
		pinto.execute("thing 1.2 + def(thing2)");
		assertEquals("Nested save ref", 3.4, runDoubleDataStatement("thing2 thing +" + EVAL)[0][0], 0.001d);
		pinto.execute("2 def(thing)");
		assertEquals("Update nested ref", 5.2, runDoubleDataStatement("thing2 thing +" + EVAL)[0][0], 0.001d);
		pinto.execute("1 + def(increment)");
		assertEquals("Saved function", 7.0, runDoubleDataStatement("6 increment" + EVAL)[0][0], 0.001d);
	}

	@Test
	public void testCopy() throws Exception {
		log.info("START testDup");
		double[][] d = runDoubleDataStatement("1 2 copy copy +" + EVAL);
		assertEquals("Correct # dup outputs", 7, d.length);
		assertEquals("Dup output works in plus", 3.0, d[d.length-1][0], 0.001d);
		d = runDoubleDataStatement("1 2 copy(3)" + EVAL);
		assertEquals("Correct # dup outputs with params", 6, d.length);

	}


	@Test(expected=PintoSyntaxException.class)
	public void testDelete() throws Exception {
		pinto.execute("1 save(deleteme)");
		pinto.execute("1 del(deleteme)");
		pinto.execute("deleteme save(doesntmatter)");
	}

	@Test(expected=Exception.class)
	public void testDeleteFail() throws Exception {
		pinto.execute("1 save(deleteme)");
		pinto.execute("deleteme 1 + del(needsdeleteme)");
		pinto.execute("1 del(deleteme)");
	}
	
	
	@Test
	public void testLabel() throws Exception {
		//bbg(msft equity,px open:px high:px low:px last) label(a,b,c,d) dup index(d) eval(2016-01-05,2016-01-05)
	}
	
	
	public void testRolling() throws Exception {
		double[][] d = runDoubleDataStatement("moon r_mean(10) eval(2016-09-06,2016-09-09,B)");
		assertEquals("rolling mean", 12.0096, d[0][d[0].length-1],0.001d);

		d = runDoubleDataStatement("moon r_mean(10,B) eval(2016-08-06,2016-09-09,W-FRI)");
		assertEquals("rolling mean (diff freqs window < range)", -33.2738, d[0][d[0].length-2],0.001d);

		d = runDoubleDataStatement("moon lag(1,BM) eval(2016-08-06,2016-09-09,B)");
		assertEquals("lag (diff freqs range > window)", -27.3281, d[0][0],0.001d);
	}
	
	public void testExpanding() throws Exception {
		double[][] d = runDoubleDataStatement("1 e_sum(2016-09-14) eval(2016-09-12,2016-09-23,B)");
		assertEquals("NAs before expanding start", Double.NaN, d[0][0],0.001d);
		assertEquals("sum from expanding start", Double.NaN, d[0][d.length - 1],0.001d);
		
	}
	
	/**
	 *   July 2014
	 *   
	 *   M  T  W  Th  F  
	 *      1  2  3   4
	 *   7  8  9  10  11
	 *   14 15
	 *   
	 * 	 	   1  2  3  4  7  8   9   10   11  14 15
	 *                    |------- t --------|
	 *        |-----b-------|   |-c-|     |----d----|  
	 *        
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 */
	private String A = "eval(2014-07-07,2014-07-11,B)";
	private String B = "eval(2014-07-01,2014-07-07,B)";
	private String C = "eval(2014-07-09,2014-07-09,B)";
	private String D = "eval(2014-07-11,2014-07-15,B)";
	@Test
	public void testCaching() throws Exception {
		String formula = "counter ";
		// 5 * 0.0
		assertEquals("Range B", 0.0, Arrays.stream(runDoubleDataStatement(formula + B)[0]).sum(), 0.001d);
		// 1 * 1.0
		assertEquals("Range C", 1.0, Arrays.stream(runDoubleDataStatement(formula + C)[0]).sum(), 0.001d);
		// 3 * 2.0
		assertEquals("Range D", 6.0, Arrays.stream(runDoubleDataStatement(formula + D)[0]).sum(), 0.001d);
		// 0, 3, 1, 4, 2
		assertEquals("Range A", 10.0, Arrays.stream(runDoubleDataStatement(formula + A)[0]).sum(), 0.001d);
		
		log.info("END testRanges");
	}

	private double[][] runDoubleDataStatement(String line) throws Exception {
		List<Response> output = pinto.execute(line);
		List<TimeSeries> dd = output.stream().map(Pinto.Response::getTimeseriesOutput).filter(Optional::isPresent)
								.map(Optional::get).flatMap(List::stream).collect(Collectors.toList());
		if(dd.size() > 0) {
			PeriodicRange<?> range = dd.get(0).getRange();
			double[][] table = new double[dd.size()][(int) range.size()];
			for(AtomicInteger i = new AtomicInteger(0); i.get() < dd.size();i.incrementAndGet()) {
				AtomicInteger j = new AtomicInteger(0);
				dd.get(i.get()).stream().forEach(
						d -> table[i.get()][j.getAndIncrement()] = d);
			}
			return table;
		} else {
			return null;
		}
			
	}


}
