package tech.pinto.tests;

import static tech.pinto.Pinto.toTableConsumer;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import tech.pinto.Cache;
import tech.pinto.Column;
import tech.pinto.MarketData;
import tech.pinto.Name;
import tech.pinto.Namespace;
import tech.pinto.Pinto;
import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;

@RunWith(Suite.class)
@SuiteClasses({
	DateTester.class,
	FunctionTester.class,
        IndexTester.class,
        SyntaxTester.class,
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
		@Provides @Singleton MarketData provideMarketData()  {
			return new MarketData() {};
		}
	}
	
	@Component(modules = TestModule.class)
	@Singleton
	public interface TestComponent {
	    Pinto pinto(); 
	    Namespace namespace();
	}
	
	public static class TestVocabulary extends StandardVocabulary {
		private static AtomicInteger count = new AtomicInteger();
		
		public TestVocabulary() {
			names.put("counter", new Name("counter", toTableConsumer(s -> {
				s.addFirst(new Column.OfDoubles(inputs -> "", inputs -> range -> {
					return Cache.getCachedValues("counter", range, 0, 1, r -> {
						Column.OfConstantDoubles col = new Column.OfConstantDoubles(count.getAndIncrement());
						col.setRange(r);
						return new double[][] {col.rows().toArray()};
					});
					
				}));
			}), "[]", "For cache testing"));
		}

	}

}
