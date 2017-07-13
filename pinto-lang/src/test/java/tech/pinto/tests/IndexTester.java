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

	/*@Test
	public void testReverse() throws Exception {
		List<TimeSeries> ts = pinto.execute("1 2 3 rev [~] label(c,b,a) eval").get(0).getColumnValues().get();
		assertEquals("reverse index (simple) label", ts.get(2).getLabel(),"a");
		assertEquals("reverse index (simple) value", ts.get(2).stream().toArray()[0],1.0,0.1);
		ts = pinto.execute("1 2 3 rev [~0:2] label(b,a) eval").get(0).getColumnValues().get();
		assertEquals("reverse index (with number index)", "a", ts.get(1).getLabel());
	}*/

	@Test
	public void testNumbers() throws Exception {
		Table ts = pinto.execute("1 2 3 [0] eval").get(0);
		assertEquals("number index (simple) value", 3.0, first(0,ts),0.1);
		ts = pinto.execute("1 2 3 [-1] eval").get(0);
		assertEquals("number index (neg) value",1.0, first(0,ts),0.1);
		ts = pinto.execute("1 2 3 [1:2] eval").get(0);
		assertEquals("number index (range) value",2.0, first(0,ts),0.1);
		ts = pinto.execute("1 2 3 [-3:-1] eval").get(0);
		assertEquals("number index (range w/ neg) value",2.0, first(1,ts),0.1);
		ts = pinto.execute("1 2 3 [2,1,0] eval").get(0);
		assertEquals("number index (list) value", 2.0, first(1,ts),0.1);
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
	
	

	private double sumRow(int row, Table t) {
		return Arrays.stream(t.toRowMajorArray().get()[0]).sum();
	}
	
	private double first(int column, Table t) {
		return t.getSeries(column).get().limit(1).sum();
	}


}
