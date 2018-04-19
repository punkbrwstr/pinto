package tech.pinto.extras;


import java.io.IOException;



import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import jline.TerminalFactory;
import tech.pinto.MarketData;
import tech.pinto.Namespace;
import tech.pinto.Pinto;
import tech.pinto.Vocabulary;

public class Main extends tech.pinto.Main {
	
	private final ExtrasComponent component;

	public Main() {
		component = DaggerMain_ExtrasComponent.builder()
				.extrasModule(new ExtrasModule())
				.build();
	}
	
	@Override
	protected Pinto getPinto() {
		return component.pinto();
	}

	public static void main(String[] args) throws IOException {
		try {
			new Main().run();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				TerminalFactory.get().restore();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	@Module
	public static class ExtrasModule {
		@Provides
		@Singleton
		Namespace provideNamespace(Vocabulary vocabulary) {
			return new Namespace(vocabulary);
		}

		@Provides
		@Singleton
		Vocabulary provideVocabulary() {
			return new ExtraVocabulary();
		}

		@Provides
		@Singleton
		MarketData provideMarketData() {
			return new BloombergMarketData();
		}
	}

	@Component(modules = ExtrasModule.class)
	@Singleton
	public interface ExtrasComponent {
		Pinto pinto();
	}

}
