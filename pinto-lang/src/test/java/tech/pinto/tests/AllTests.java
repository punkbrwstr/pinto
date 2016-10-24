package tech.pinto.tests;

import java.util.Arrays;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;

import javax.inject.Singleton;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import tech.pinto.Namespace;
import tech.pinto.Pinto;
import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.function.Function;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.supplier.CachedSupplierFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

@RunWith(Suite.class)
@SuiteClasses({
	DateTester.class,
	StatementTester.class
})
public class AllTests {
	
	public static TestComponent component;
	@BeforeClass
	public static void initialize() throws InterruptedException {
		component = DaggerAllTests_TestComponent.builder()
			    .testModule(new TestModule()) .build();
	}

	@AfterClass
	public static void shutdown() {

	}
	
	@Module
	public static class TestModule {
		@Provides @Singleton Vocabulary provideVocabulary()  {
			return new TestVocabulary();
		}
	}
	
	@Component(modules = TestModule.class)
	@Singleton
	public interface TestComponent {
	    Pinto pinto(); 
	    Namespace namespace();
	}
	
	public static class TestVocabulary extends StandardVocabulary {
		
		public TestVocabulary() {
			names.put("counter", new tech.pinto.Name((n,c,i,s,a) -> new CallCounter(n,i,a), n -> new FunctionHelp.Builder(n).build()));
		}

	}
	
	public static class CallCounter extends CachedSupplierFunction {
		
		private static AtomicInteger count = new AtomicInteger();

		public CallCounter(String name, LinkedList<Function> inputs, String...args) {
			super(name, inputs, args);
		}

		@Override
		public <P extends Period> List<DoubleStream> evaluateAll(PeriodicRange<P> range) {
			double d = count.getAndIncrement();
			return Arrays.asList(DoubleStream.iterate(d, r -> d ).limit(range.size()));
		}

		@Override
		protected int additionalOutputCount() {
			return 1;
		}

		@Override
		protected List<String> allLabels() {
			return Arrays.asList("counter");
		}


	}



}
