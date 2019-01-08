package tech.pinto.tests;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
import tech.pinto.Pinto.StackFunction;
import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

@RunWith(Suite.class)
@SuiteClasses({
	DateTester.class,
	FunctionTester.class,
    IndexTester.class,
    SyntaxTester.class,
	WindowTester.class
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
			return new MarketData() {
				@Override public <P extends Period<P>> Function<PeriodicRange<?>, double[][]> getRowFunction(Request request) { throw new UnsupportedOperationException(); }
				@Override public String getDefaultField() { throw new UnsupportedOperationException(); }};
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
			Cache.putFunction("counter", 1, r -> {
				var d = new double[(int) r.size()];
				Arrays.fill(d, count.getAndIncrement());
				return new double[][] {d};
			});
			names.add(Name.nameBuilder("counter", (StackFunction) (p,s) -> {
				s.addFirst(new Column<double[]>(double[].class, inputs -> "", (range, inputs) -> {
					return Cache.getCachedRows("counter", 0, range);
					
				}));
			}));
		}

	}

}
