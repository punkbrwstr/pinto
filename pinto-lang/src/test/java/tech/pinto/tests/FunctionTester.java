package tech.pinto.tests;

import org.junit.BeforeClass;

import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.TerminalFunction;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
	public void testSave() throws Exception {
		executeVoid("1.1 \"thing\" def");
		executeVoid("thing 1.2 + \"thing2\" def");
		assertEquals("Nested save ref", 3.4, execute("thing2 thing + eval").sumFirstRow(), 0.001d);
		executeVoid("2 \"thing\" def");
		assertEquals("Update nested ref", 5.2, execute("thing2 thing +" + EVAL).sumFirstRow(), 0.001d);
		executeVoid("1 + \"increment\" def");
		assertEquals("Saved function", 7.0, execute("6 increment" + EVAL).sumFirstRow(), 0.001d);
	}

	@Test
	public void testCopy() throws Exception {
		Table t = execute("1 2 copy copy +" + EVAL);
		assertEquals("Correct # dup outputs", 7, t.getSeries().size());
		assertEquals("Dup output works in plus", 3.0, t.getSeries().get(6).get()[0], 0.001d);
		t = execute("1 2 \"3\" copy" + EVAL);
		assertEquals("Correct # dup outputs with params", 6, t.getSeries().size());

	}


	@Test(expected=PintoSyntaxException.class)
	public void testDelete() throws Exception {
		pinto.execute("1 \"deleteme\" def");
		pinto.execute("\"deleteme\" del");
		pinto.execute("deleteme \"blah\" def");
	}

	@Test(expected=Exception.class)
	public void testDeleteFail() throws Exception {
		pinto.execute("1 \"deleteme\" def");
		pinto.execute("deleteme 1 + \"needsdeleteme\" del");
		pinto.execute("\"deleteme\" del");
	}
	
	@Test
	public void testNoInputsToDefined() throws Exception {
		executeVoid("2 \"20\" r_mean \"a, [x],test\" def");
		executeVoid("3 \"30\" r_mean \"b, [x],test\" def");
		executeVoid("a b \"c\" def");
		Table  c = execute("c eval");
		assertEquals("defineNoInputs count",c.getSeries().size(),2);
		assertEquals("definedNoInputs output",c.sumFirstRow(),5.0,0.01);
	}
	
	
	public void testRolling() throws Exception {
		Table d = execute("moon \"10\" r_mean \"2016-09-06,2016-09-09,B\" eval");
		assertEquals("rolling mean", 12.0096, d.getSeries().get(d.getSeries().size()-1).get()[0],0.001d);

		d = execute("moon \"10,B\" r_mean \"2016-08-06,2016-09-09,W-FRI \"eval");
		assertEquals("rolling mean (diff freqs window < range)", -33.2738, d.getSeries().get(d.getSeries().size()-2).get()[0],0.001d);

		d = execute("moon \"1,BM\" lag \"2016-08-06,2016-09-09,B\" eval");
		assertEquals("lag (diff freqs range > window)", -27.3281, d.getSeries().get(0).get()[0],0.001d);
	}
	
	public void testExpanding() throws Exception {
		Table d = execute("1 \"2016-09-14\" e_sum \"2016-09-12,2016-09-23,B\" eval");
		assertEquals("NAs before expanding start", Double.NaN, d.sumFirstRow(),0.001d);
		assertEquals("sum from expanding start", 8.0, d.getSeries().get(0).get()[d.getRows() - 1],0.001d);
		
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
		assertEquals("Range B", 0.0, execute(formula + B).sumFirstColumn(), 0.001d);
		// 1 * 1.0
		assertEquals("Range C", 1.0, execute(formula + C).sumFirstColumn(), 0.001d);
		// 3 * 2.0
		assertEquals("Range D", 6.0, execute(formula + D).sumFirstColumn(), 0.001d);
		// 0, 3, 1, 4, 2
		assertEquals("Range A", 10.0, execute(formula + A).sumFirstColumn(), 0.001d);
		
		log.info("END testRanges");
	}
	
	private void executeVoid(String s) throws Exception {
		pinto.execute(s).get(0).getColumnValues().get(0).getHeader();
	}

	private Table execute(String line) throws Exception {
		Table table = new Table();
		TerminalFunction tf = pinto.execute(line).get(0);
		tf.getColumnValues().forEach((cv) -> {
			
			table.getHeader().add(cv.getHeader());
			table.getSeries().add(cv.getSeries().isPresent() ? Optional.of(cv.getSeries().get().toArray()): Optional.empty());
		});
		return table;
		
	}
	
	public static class Table {
		List<Optional<String>> header = new ArrayList<>();
		List<Optional<double[]>> series = new ArrayList<>();
		int rows;
		
		public List<Optional<String>> getHeader() {
			return header;
		}
		public List<Optional<double[]>> getSeries() {
			return series;
		}
		
		public void setRows(int i) {
			rows = i;
		}
		
		public int getRows() { 
			return rows;
		}
		
		public int getColums() {
			return header.size();
		}
		
		public double sumFirstColumn() {
			return Arrays.stream(series.get(0).get()).sum();
		}
		
		public double sumFirstRow() {
			return sumIthRow(0);
		}

		public double sumIthRow(int i) {
			return series.stream().mapToDouble(od -> od.isPresent() ? od.get()[i] : 0.0).sum();
		}
	}


}
