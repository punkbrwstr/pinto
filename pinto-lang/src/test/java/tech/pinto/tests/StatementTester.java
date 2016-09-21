package tech.pinto.tests;

import org.junit.BeforeClass;
import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.PintoSyntaxException;
import tech.pinto.data.Data;
import tech.pinto.data.DoubleData;
import tech.pinto.time.PeriodicRange;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
		pinto.evaluateStatement("1.1 save(thing)");
		pinto.evaluateStatement("thing 1.2 + save(thing2)");
		assertEquals("Nested save ref", 3.4, runDoubleDataStatement("thing thing2 +" + EVAL)[0][0], 0.001d);
		pinto.evaluateStatement("2 save(thing)");
		assertEquals("Update nested ref", 5.2, runDoubleDataStatement("thing thing2 +" + EVAL)[0][0], 0.001d);
		pinto.evaluateStatement("1 + save(increment)");
		assertEquals("Saved function", 7.0, runDoubleDataStatement("6 increment" + EVAL)[0][0], 0.001d);
	}

	@Test
	public void testDup() throws Exception {
		log.info("START testDup");
		double[][] d = runDoubleDataStatement("1 2 dup dup +" + EVAL);
		assertEquals("Correct # dup outputs", 7, d.length);
		assertEquals("Dup output works in plus", 3.0, d[d.length-1][0], 0.001d);

	}


	@Test(expected=PintoSyntaxException.class)
	public void testDelete() throws Exception {
		pinto.evaluateStatement("1 save(deleteme)");
		pinto.evaluateStatement("1 del(deleteme)");
		pinto.evaluateStatement("deleteme save(doesntmatter)");
	}

	@Test(expected=Exception.class)
	public void testDeleteFail() throws Exception {
		pinto.evaluateStatement("1 save(deleteme)");
		pinto.evaluateStatement("deleteme 1 + del(needsdeleteme)");
		pinto.evaluateStatement("1 del(deleteme)");
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
		ArrayDeque<Data<?>> output = (ArrayDeque<Data<?>>) pinto.evaluateStatement(line);
		List<DoubleData> dd = new ArrayList<>();
		while(!output.isEmpty()) {
			if(output.peekFirst() instanceof DoubleData) {
				dd.add((DoubleData) output.removeFirst());
			}
		}
		if(dd.size() > 0) {
			PeriodicRange<?> range = dd.get(0).getRange();
			double[][] table = new double[dd.size()][(int) range.size()];
			for(AtomicInteger i = new AtomicInteger(0); i.get() < dd.size();i.incrementAndGet()) {
				AtomicInteger j = new AtomicInteger(0);
				dd.get(i.get()).getData().forEach(
						d -> table[i.get()][j.getAndIncrement()] = d);
			}
			return table;
		} else {
			return null;
		}
			
	}


}
