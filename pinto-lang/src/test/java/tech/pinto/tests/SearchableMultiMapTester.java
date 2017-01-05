package tech.pinto.tests;




import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableMap;

import tech.pinto.tools.SearchableMultiMap;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchableMultiMapTester {

	@SuppressWarnings("unused")
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testOperations() {
		SearchableMultiMap<String> testMap = new SearchableMultiMap<>();
		testMap.add("dog", "A");
		testMap.add("hotdog", "B");
		testMap.add("hotdog", "C");
		testMap.add("doghouse", "D");
		testMap.add("hotdiggittydog", "E");
		testMap.add("doghouseboat", "F");
		testMap.add("chicagohotdog", "G");
		
		Map<String,String> tests = new ImmutableMap.Builder<String, String>()
				.put("*","G,A,D,F,E,B,C")
				.put("dog","A")
				.put("hotdog","B,C")
				.put("hotdog*","B,C")
				.put("doghouse*","D,F")
				.put("*hotdog","G,B,C")
				.put("hot*dog","E,B,C")
				.put("hamburger","none")
				.put("bagel*dog","none")
				.put("hot*bagel","none")
				.put("bagel*","none")
				.put("*bagel","none")
				.build();
		
		for(Map.Entry<String, String> e : tests.entrySet()) {
			assertEquals(e.getKey(), e.getValue(), testMap.search(e.getKey()).orElse(Arrays.asList("none"))
					.stream().collect(Collectors.joining(",")));
		}
	}
	
	

}
