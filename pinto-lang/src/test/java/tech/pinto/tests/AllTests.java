package tech.pinto.tests;

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
import tech.pinto.LocalCache;
import tech.pinto.Pinto;
import tech.pinto.Vocabulary;

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
		@Provides @Singleton Cache provideCache(Vocabulary vocabulary)  {
			return new LocalCache(vocabulary);
		}
		@Provides @Singleton Vocabulary provideVocabulary()  {
			return new TestVocabulary();
		}
	}
	
	@Component(modules = TestModule.class)
	@Singleton
	public interface TestComponent {
	    Pinto pinto(); 
	}


}
