package tech.pinto.tests;

import java.util.Arrays;

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
import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Pinto;
import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.function.CachedFunction;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

@RunWith(Suite.class)
@SuiteClasses({
	DateTester.class,
	FunctionTester.class,
        IndexTester.class,
	SearchableMultiMapTester.class
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
			names.put("counter", new tech.pinto.Name((n,p,s,f,i,a) -> new CallCounter(n,f,i,a), n -> new FunctionHelp.Builder(n).build()));
		}

	}
	
	public static class CallCounter extends CachedFunction {
		
		private static AtomicInteger count = new AtomicInteger();

		public CallCounter(String name, ComposableFunction previousFunction, Indexer indexer, String... args) {
			super(name, previousFunction, indexer, args);
		}

		@Override
		protected <P extends Period> List<DoubleStream> getUncachedSeries(PeriodicRange<P> range) {
			double d = count.getAndIncrement();
			return Arrays.asList( DoubleStream.iterate(d, r -> d ).limit(range.size()));
		}

		@Override
		protected int columns() {
			return 1;
		}

		@Override
		protected List<String> getUncachedText() {
			return Arrays.asList("counter");
		}

	}



}
