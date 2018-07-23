package tech.pinto.tests;

import org.junit.BeforeClass;


import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.Table;

import static org.junit.Assert.*;

public class IndexTester {

	@SuppressWarnings("unused")
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	private static Pinto pinto;

	@BeforeClass
	public static void setup() {
		pinto = AllTests.component.pinto();
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testWildcards() throws Exception {
		Table t = pinto.evaluate("1 2 3 {hotdiggitydog,burger,hotdog} [hot*dog] only eval").get(0);
		assertEquals("wildcard label", 2, t.getColumnCount());
	}

	@Test
	public void testOrdinals() throws Exception {
		assertTrue("index by ordinal", compareRow("range [0] only eval", 3.0));
		assertTrue("index by negative ordinal", compareRow("range [-1] only eval", 1.0d));
		assertTrue("index by multiple ordinal", compareRow("range [-1,0] only eval", 1.0d, 3.0d));
	}

	public void testSlices() throws Exception {
		assertTrue("index by slice", compareRow("range [0:2] only eval", 4.0d, 3.0d));
	}

	@Test
	public void testLabels() throws Exception {
		Table ts = pinto.evaluate("1 2 3 {a,b,c} [c] eval").get(0);
		assertEquals("label index (simple) value", 3.0, first(0,ts), 0.1);
	}

	@Test
	public void testCopyAndRepeat() throws Exception {
		pinto.evaluate(":function_that_copies [&0] 1 + def");
		Table t = pinto.evaluate("98 99 [0+] function_that_copies eval").get(0);
		assertEquals("Repeat a defined that copies", t.getColumnCount(),4);
		assertEquals("Repeat a defined that copies", first(1,t),99, 0.1);
	}
	
	@Test
	public void repeatWithNested() throws Exception {
		pinto.evaluate(":test [0] ([0&] ) + def");
		Table t = pinto.evaluate("1 2 [0+] test eval").get(0);
		assertEquals("Repeat a defined with nested inline", t.getColumnCount(),2);
		assertEquals("Repeat a defined with nested inline", first(0,t),4, 0.1);
	}
	
	private double first(int column, Table t) {
		return t.toColumnMajorArray()[column][0];
	}
	
	private boolean compareRow(String pintoExpression, double... results) throws Exception {
		return compareRow(results,pinto.evaluate(pintoExpression).get(0));
	}

	private boolean compareRow(double[] target, Table t) {
		double[] result = t.toRowMajorArray()[0];
		for(int i = 0; i < result.length; i++) {
			if(Math.abs(result[i] - target[i]) > 0.00001) {
				return false;
			}
		}
		return true;
	}


}
