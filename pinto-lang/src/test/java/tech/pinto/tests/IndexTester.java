package tech.pinto.tests;

import org.junit.BeforeClass;


import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.Table;

import static org.junit.Assert.*;

import java.util.Arrays;

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
		Table t = pinto.execute("1 2 3 \"hotdiggitydog,burger,hotdog\" label [hot*dog] only eval").get(0);
		assertEquals("wildcard label", 2, t.getColumnCount());
	}

	@Test
	public void testOrdinals() throws Exception {
		assertTrue("index by ordinal", compareRow("range [0] only eval", 4.0));
		assertTrue("index by negative ordinal", compareRow("range [-1] only eval", 0.0d));
		assertTrue("index by multiple ordinal", compareRow("range [-1,2] only eval", 2.0d, 0.0d));
		assertTrue("index by multiple same ordinal", compareRow("range [-1,4] only eval", 0.0d, 0.0d));
	}

	public void testSlices() throws Exception {
		assertTrue("index by slice", compareRow("range [0:2] only eval", 4.0d, 3.0d));
	}

	@Test
	public void testLabels() throws Exception {
		Table ts = pinto.execute("1 2 3 \"a,b,c\" label [c] eval").get(0);
		assertEquals("label index (simple) value", 3.0, first(0,ts), 0.1);
		ts = pinto.execute("1 2 3 \"a,b,c\" label [b,b] neg eval").get(0);
		assertEquals("repeated label index", sumRow(0,ts),0.0,0.1);
		ts = pinto.execute("1 2 3 \"b,b,a\" label [b] neg eval").get(0);
		assertEquals("label index (repeated label) value", sumRow(0,ts),0.0,0.1);
		
	}

	@Test
	public void testCopyAndRepeat() throws Exception {
		pinto.execute("[&0] 1 + \"function_that_copies\" def");
		Table t = pinto.execute("98 99 [0+] function_that_copies eval").get(0);
		assertEquals("Repeat a defined that copies", t.getColumnCount(),4);
		assertEquals("Repeat a defined that copies", first(1,t),99, 0.1);
	}
	
	

	private double sumRow(int row, Table t) {
		return Arrays.stream(t.toRowMajorArray().get()[0]).sum();
	}
	
	private double first(int column, Table t) {
		return t.getSeries(column).get().limit(1).sum();
	}
	
	private boolean compareRow(String pintoExpression, double... results) throws Exception {
		return compareRow(results,pinto.execute(pintoExpression).get(0));
	}

	private boolean compareRow(double[] target, Table t) {
		double[] result = t.toRowMajorArray().get()[0];
		for(int i = 0; i < result.length; i++) {
			if(Math.abs(result[i] - target[i]) > 0.00001) {
				return false;
			}
		}
		return true;
	}


}
