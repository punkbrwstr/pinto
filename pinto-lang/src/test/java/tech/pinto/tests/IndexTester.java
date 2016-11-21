package tech.pinto.tests;

import org.junit.BeforeClass;

import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.TimeSeries;
import tech.pinto.time.PeriodicRange;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;

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
	public void testReverse() throws Exception {
		List<TimeSeries> ts = pinto.execute("1 2 3 rev [~] label(a,b,c) eval").get(0).getTimeSeries().get();
		assertEquals("reverse index (simple) label", ts.get(2).getLabel(),"a");
		assertEquals("reverse index (simple) value", ts.get(2).stream().toArray()[0],1.0,0.1);
		ts = pinto.execute("1 2 3 rev [~0:1] label(a,b) eval").get(0).getTimeSeries().get();
		assertEquals("reverse index (with number index)", ts.get(2).getLabel(),"a");
	}

	@Test
	public void testNumbers() throws Exception {
		List<TimeSeries> ts = pinto.execute("1 2 3 [0] eval").get(0).getTimeSeries().get();
		assertEquals("number index (simple) value", ts.get(0).stream().toArray()[0],3.0,0.1);
		ts = pinto.execute("1 2 3 [-1] eval").get(0).getTimeSeries().get();
		assertEquals("number index (neg) value", ts.get(0).stream().toArray()[0],1.0,0.1);
		ts = pinto.execute("1 2 3 [1:2] eval").get(0).getTimeSeries().get();
		assertEquals("number index (range) value", ts.get(0).stream().toArray()[0],2.0,0.1);
		ts = pinto.execute("1 2 3 [-3:-1] eval").get(0).getTimeSeries().get();
		assertEquals("number index (range w/ neg) value", ts.get(1).stream().toArray()[0],2.0,0.1);
		ts = pinto.execute("1 2 3 [2,1,0] eval").get(0).getTimeSeries().get();
		assertEquals("number index (list) value", ts.get(1).stream().toArray()[0],1.0,0.1);
		ts = pinto.execute("1 2 3 rev [~] label(a,b,c) [1,1] neg eval").get(0).getTimeSeries().get();
		double sum = ts.stream().map(TimeSeries::stream).map(DoubleStream::toArray).mapToDouble(a -> a[0]).sum();
		assertEquals("number index (list) value", sum,0.0,0.1);


	}

	@Test
	public void testLabels() throws Exception {
		List<TimeSeries> ts = pinto.execute("1 2 3 rev [~] label(a,b,c) [c] eval").get(0).getTimeSeries().get();
		assertEquals("label index (simple) value", ts.get(1).stream().toArray()[0],3.0,0.1);
		ts = pinto.execute("1 2 3 rev [~] label(a,b,c) [b,b] neg eval").get(0).getTimeSeries().get();
		double sum = ts.stream().map(TimeSeries::stream).map(DoubleStream::toArray).mapToDouble(a -> a[0]).sum();
		assertEquals("label index (get one twice) value", sum,0.0,0.1);
		ts = pinto.execute("1 2 3 rev [~] label(a,b,b) [b] neg eval").get(0).getTimeSeries().get();
		sum = ts.stream().map(TimeSeries::stream).map(DoubleStream::toArray).mapToDouble(a -> a[0]).sum();
		assertEquals("label index (repeated label) value", sum,-4.0,0.1);
		
	}

	private double[][] run(String line) throws Exception {
		List<TimeSeries> dd = pinto.execute(line).get(0).getTimeSeries().get();
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
