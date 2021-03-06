package tech.pinto.tests;

import org.junit.BeforeClass;

import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;

import static org.junit.Assert.*;

import java.util.Arrays;

public class FunctionTester {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	private final String EVAL = " eval";
	private static Pinto pinto;

	@BeforeClass
	public static void setup() {
		pinto = AllTests.component.pinto();
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testDefine() throws Exception {
		pinto.evaluate(":thing 1.1 def");
		pinto.evaluate(":thing2 thing 1.2 + def");
		assertEquals("Nested save ref", 3.4, sumRow(0,pinto.evaluate("thing2 thing + eval").get(0)), 0.001d);
		pinto.evaluate(":thing 2 def");
		assertEquals("Update nested ref", 5.2, sumRow(0,pinto.evaluate("thing2 thing +" + EVAL).get(0)), 0.001d);
		pinto.evaluate(":increment [0] 1 + def");
		assertEquals("Saved function", 7.0, sumRow(0,pinto.evaluate("6 increment" + EVAL).get(0)), 0.001d);
	}

	@Test
	public void testCacheClearing() throws Exception {
		pinto.evaluate(":a [] 5 def");
		pinto.evaluate(":b [] a 10 + def");
		pinto.evaluate(":c [] b 5 + def");
		assertEquals("Nested defined", 20, sumRow(0,pinto.evaluate("c eval").get(0)), 0.001d);
		pinto.evaluate(":a [] 4 def");
		assertEquals("Nested defined after dependency change", 19, sumRow(0,pinto.evaluate("c eval").get(0)), 0.001d);
	}

	@Test
	public void testCopy() throws Exception {
		Table t = pinto.evaluate("{test: 1 2} copy copy +" + EVAL).get(0);
		assertEquals("Correct # dup outputs", 7, t.getColumnCount());
		assertEquals("Dup output works in plus", 3.0, t.toRowMajorArray()[0][6], 0.001d);
		t = pinto.evaluate("1 2 3 copy" + EVAL).get(0);
		assertEquals("Correct # dup outputs with params", 6, t.getColumnCount());

	}



	@Test(expected=PintoSyntaxException.class)
	public void testDelete() throws Exception {
		pinto.evaluate("1 \"deleteme\" def");
		pinto.evaluate("\"deleteme\" del");
		pinto.evaluate("deleteme \"blah\" def");
	}

	@Test(expected=Exception.class)
	public void testDeleteFail() throws Exception {
		pinto.evaluate("1 \"dontdeleteme\" def");
		pinto.evaluate("dontdeleteme 1 + \"needsdeleteme\" def");
		pinto.evaluate("\"dontdeleteme\" del");
	}
	
	@Test
	public void testNoInputsToDefined() throws Exception {
		pinto.evaluate(":a [] {test: 2} 20 rolling mean def");
		pinto.evaluate(":b [] {test: 3} 30 rolling mean def");
		pinto.evaluate(":c a b def");
		Table  c = pinto.evaluate("c eval").get(0);
		assertEquals("defineNoInputs count",c.getColumnCount(),2);
		assertEquals("definedNoInputs output",sumRow(0,c),5.0,0.01);
	}
	
	
	public void testRolling() throws Exception {
		Table d = pinto.evaluate("moon \"10\" r_mean \"2016-09-06,2016-09-09,B\" eval").get(0);
		assertEquals("rolling mean", 12.0096, d.toRowMajorArray()[3][0],0.001d);

		d =pinto.evaluate("moon \"10,B\" r_mean \"2016-08-06,2016-09-02,W-FRI \"eval").get(0);
		assertEquals("rolling mean (diff freqs window < range)", -33.2738, last(0,d),0.001d);

		d =pinto.evaluate("moon \"1,BM\" r_lag \"2016-08-06,2016-09-09,B\" eval").get(0);
		assertEquals("lag (diff freqs range > window)", -27.3281, first(0,d),0.001d);
	}
	
	public void testExpanding() throws Exception {
		Table d = pinto.evaluate("1 \"2016-09-14\" e_sum \"2016-09-12,2016-09-23,B\" eval").get(0);
		assertEquals("NAs before expanding start", Double.NaN, sumRow(0,d),0.001d);
		assertEquals("sum from expanding start", 8.0, last(0,d), 0.001d);
		
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
	private String A = "2014-07-07 2014-07-11 B eval";
	private String B = "2014-07-01 2014-07-07 B eval";
	private String C = "2014-07-09 2014-07-09 B eval";
	private String D = "2014-07-11 2014-07-15 B eval";
	@Test
	public void testCaching() throws Exception {
		String formula = "counter ";
		// 5 * 0.0
		assertEquals("Range B", 0.0, sumColumn(0,pinto.evaluate(formula + B).get(0)), 0.001d);
		// 1 * 1.0
		assertEquals("Range C", 1.0, sumColumn(0,pinto.evaluate(formula + C).get(0)), 0.001d);
		// 3 * 2.0
		assertEquals("Range D", 6.0, sumColumn(0, pinto.evaluate(formula + D).get(0)), 0.001d);
		// 0, 3, 1, 4, 2
		assertEquals("Range A", 10.0, sumColumn(0, pinto.evaluate(formula + A).get(0)), 0.001d);
		
		log.info("END testRanges");
	}
	
		
	private double sumColumn(int col, Table r) {
		return Arrays.stream(r.toColumnMajorArray()[col]).sum();
	}

	private double sumRow(int row, Table r) {
		return Arrays.stream(r.toRowMajorArray()[row]).sum();
	}

	private double first(int column, Table t) {
		return t.toColumnMajorArray()[column][0];
	}

	private double last(int column, Table t) {
		double[] d = t.toColumnMajorArray()[column];
		return d[d.length-1];
	}

}
