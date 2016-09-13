package pinto.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DateTester.class
})
public class AllTests {

	@BeforeClass
	public static void initialize() throws InterruptedException {

	}

	@AfterClass
	public static void shutdown() {

	}

}
