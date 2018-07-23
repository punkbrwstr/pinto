package tech.pinto.tests;

import org.junit.BeforeClass;


import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.Table;

import static org.junit.Assert.*;

import java.util.List;

public class WindowTester {

	private static Pinto pinto;

	@BeforeClass
	public static void setup() {
		pinto = AllTests.component.pinto();
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testExpanding() throws Exception {
		assertEquals("Expanding for range", 3.0,
				last(0, pinto.evaluate("1 ([0&] expanding sum) 2018-07-01 2018-07-04 eval")),
				0.001d);

		assertEquals("Expanding starting in middle of range", 2.0,
				last(0, pinto.evaluate("1 ([0&] 2018-07-03 expanding sum) 2018-07-01 2018-07-04 eval")),
				0.001d);

		assertTrue("Expanding starting after range",
				Double.isNaN(last(0, pinto.evaluate("1 ([0&] 2018-07-06 expanding sum) 2018-07-01 2018-07-04 eval"))));

		assertEquals("Expanding starting before range", 4.0,
				last(0, pinto.evaluate("1 ([0&] 2018-06-29 expanding sum) 2018-07-01 2018-07-04 eval")),
				0.001d);
	}

	@Test
	public void testRolling() throws Exception {
		assertEquals("Expanding for range", 3.0,
				last(0, pinto.evaluate("1 ([0&] expanding sum) 2018-07-01 2018-07-04 eval")),
				0.001d);
	}

	@Test
	public void testRevExpanding() throws Exception {
		assertEquals("Reverse expanding for range", 4.0,
				first(0, pinto.evaluate("1 rev_expanding sum 2018-07-05 2018-07-10 eval")),
				0.001d);
	}

	@Test
	public void testCross() throws Exception {
		assertEquals("Test cross", 6.0,
				last(0, pinto.evaluate("range cross sum eval")),
				0.001d);

		assertEquals("Test cross with NAs", 12.0,
				last(0, pinto.evaluate("1 2 3 NaN 1 2 3 cross sum eval")),
				0.001d);
	}

	@Test
	public void testStatistics() throws Exception {
		assertEquals("Rolling mean for moon", 1.8425,
				last(0, pinto.evaluate("moon 200 rolling mean 2018-07-02 2018-07-02 eval")),
				0.001d);

		assertEquals("Rolling std for moon", 61.48,
				last(0, pinto.evaluate("moon 200 rolling std 2018-07-02 2018-07-02 eval")),
				0.001d);

		assertEquals("Rolling zscore for moon", -1.4535,
				last(0, pinto.evaluate("moon 200 rolling zscore 2018-07-02 2018-07-02 eval")),
				0.001d);
	}

	@Test
	public void testMinMax() throws Exception {
		assertEquals("Cross max for moon", 99.9588,
				last(0, pinto.evaluate("moon ([0&] rolling first) ([0&] rolling first) ([0&] rolling first) ([0&] rolling first) ([:] cross max {max}) 2018-07-02 2018-07-02 eval")),
				0.001d);

		assertEquals("Cross min for moon", -99.4319,
				last(0, pinto.evaluate("moon ([0&] rolling first) ([0&] rolling first) ([0&] rolling first) ([0&] rolling first) ([:] cross min {min}) 2018-07-02 2018-07-02 eval")),
				0.001d);

		assertEquals("Expanding max for moon", 2.065,
				last(0, pinto.evaluate("moon ([0&] expanding max) 2018-06-11 2018-06-15 eval")),
				0.001d);
	}
		
	private double last(int column, List<Table> t) {
		double[] d = t.get(0).toColumnMajorArray()[column];
		return d[d.length-1];
	} 

	private double first(int column, List<Table> t) {
		return t.get(0).toColumnMajorArray()[column][0];
	} 

}
