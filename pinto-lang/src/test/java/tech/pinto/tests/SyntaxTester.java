package tech.pinto.tests;

import org.junit.BeforeClass;

import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.Pinto;
import tech.pinto.Table;

import static org.junit.Assert.*;

public class SyntaxTester {

//	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	private static Pinto pinto;

	@BeforeClass
	public static void setup() {
		pinto = AllTests.component.pinto();
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void nestedIndex() throws Exception {
		Table t = pinto.eval(" [n=10] range [3,2,z= 99 .5 [1] only ] eval").get(0);
		assertEquals("Nested index in index text", "z", t.getHeaders(true).get(0));
		assertEquals("Nested index in index value", 99.0, t.getSeries(0, true).findFirst().getAsDouble(),0.01);
	}

	@Test
	public void inlineFunction() throws Exception {
		Table t = pinto.eval(" 1 2 3 ([0] 1 + 1 +) 4 eval").get(0);
		assertEquals("Inline function", 5.0, t.getSeries(1, false).findFirst().getAsDouble(),0.01);
		assertEquals("Inline function", 1.0, t.getSeries(0, true).findFirst().getAsDouble(),0.01);
	}


	@Test
	public void extraCommasInHeaderLiteral() throws Exception {
		Table t = pinto.eval("{x : 1 2 3 [0,1], y : 5} eval").get(0);
		assertEquals("Extra commas in header literal", 3, t.getColumnCount());
		assertEquals("Extra commas in header literal", "x", t.getHeaders(true).get(0));
		assertEquals("Extra commas in header literal", "y", t.getHeaders(false).get(0));
		assertEquals("Inline function", 5.0, t.getSeries(0, false).findFirst().getAsDouble(),0.01);
		assertEquals("Inline function", 2.0, t.getSeries(0, true).findFirst().getAsDouble(),0.01);
	}

}
