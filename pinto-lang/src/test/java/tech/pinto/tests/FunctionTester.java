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
		pinto.execute("1.1 \"thing\" def");
		pinto.execute("thing 1.2 + \"thing2\" def");
		assertEquals("Nested save ref", 3.4, sumRow(0,pinto.execute("thing2 thing + eval").get(0)), 0.001d);
		pinto.execute("2 \"thing\" def");
		assertEquals("Update nested ref", 5.2, sumRow(0,pinto.execute("thing2 thing +" + EVAL).get(0)), 0.001d);
		pinto.execute("1 + \"increment\" def");
		assertEquals("Saved function", 7.0, sumRow(0,pinto.execute("6 increment" + EVAL).get(0)), 0.001d);
	}

	@Test
	public void testCopy() throws Exception {
		Table t = pinto.execute("1 2 copy copy +" + EVAL).get(0);
		assertEquals("Correct # dup outputs", 7, t.getColumnCount());
		assertEquals("Dup output works in plus", 3.0, t.toRowMajorArray().get()[0][6], 0.001d);
		t = pinto.execute("1 2 \"3\" copy" + EVAL).get(0);
		assertEquals("Correct # dup outputs with params", 6, t.getColumnCount());

	}


	@Test(expected=PintoSyntaxException.class)
	public void testDelete() throws Exception {
		pinto.execute("1 \"deleteme\" def");
		pinto.execute("\"deleteme\" del");
		pinto.execute("deleteme \"blah\" def");
	}

	@Test(expected=Exception.class)
	public void testDeleteFail() throws Exception {
		pinto.execute("1 \"dontdeleteme\" def");
		pinto.execute("dontdeleteme 1 + \"needsdeleteme\" def");
		pinto.execute("\"dontdeleteme\" del");
	}
	
	@Test
	public void testNoInputsToDefined() throws Exception {
		pinto.execute("2 \"20\" r_mean \"a, [x],test\" def");
		pinto.execute("3 \"30\" r_mean \"b, [x],test\" def");
		pinto.execute("a b \"c\" def");
		Table  c = pinto.execute("c eval").get(0);
		assertEquals("defineNoInputs count",c.getColumnCount(),2);
		assertEquals("definedNoInputs output",sumRow(0,c),5.0,0.01);
	}
	
	
	public void testRolling() throws Exception {
		Table d = pinto.execute("moon \"10\" r_mean \"2016-09-06,2016-09-09,B\" eval").get(0);
		assertEquals("rolling mean", 12.0096, d.toRowMajorArray().get()[3][0],0.001d);

		d =pinto.execute("moon \"10,B\" r_mean \"2016-08-06,2016-09-02,W-FRI \"eval").get(0);
		assertEquals("rolling mean (diff freqs window < range)", -33.2738, last(0,d),0.001d);

		d =pinto.execute("moon \"1,BM\" r_lag \"2016-08-06,2016-09-09,B\" eval").get(0);
		assertEquals("lag (diff freqs range > window)", -27.3281, first(0,d),0.001d);
	}
	
	public void testExpanding() throws Exception {
		Table d = pinto.execute("1 \"2016-09-14\" e_sum \"2016-09-12,2016-09-23,B\" eval").get(0);
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
	private String A = "\"2014-07-07,2014-07-11,B\" eval";
	private String B = "\"2014-07-01,2014-07-07,B\" eval";
	private String C = "\"2014-07-09,2014-07-09,B\" eval";
	private String D = "\"2014-07-11,2014-07-15,B\" eval";
	@Test
	public void testCaching() throws Exception {
		String formula = "counter ";
		// 5 * 0.0
		assertEquals("Range B", 0.0, sumColumn(0,pinto.execute(formula + B).get(0)), 0.001d);
		// 1 * 1.0
		assertEquals("Range C", 1.0, sumColumn(0,pinto.execute(formula + C).get(0)), 0.001d);
		// 3 * 2.0
		assertEquals("Range D", 6.0, sumColumn(0, pinto.execute(formula + D).get(0)), 0.001d);
		// 0, 3, 1, 4, 2
		assertEquals("Range A", 10.0, sumColumn(0, pinto.execute(formula + A).get(0)), 0.001d);
		
		log.info("END testRanges");
	}
	
		
	private double sumColumn(int col, Table t) {
		return t.getSeries(col).sum();
	}

	private double sumRow(int row, Table t) {
		return Arrays.stream(t.toRowMajorArray().get()[0]).sum();
	}
	
	private double first(int column, Table t) {
		return t.getSeries(column).limit(1).sum();
	}

	private double last(int column, Table t) {
		return t.getSeries(column).skip(t.getRowCount()-1).sum();
	}


}
