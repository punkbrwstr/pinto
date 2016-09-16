package tech.pinto.extras;


import java.io.IOException;


import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import jline.TerminalFactory;
import tech.pinto.Cache;
import tech.pinto.Console;
import tech.pinto.LocalCache;
import tech.pinto.Pinto;
import tech.pinto.Vocabulary;

public class ExtrasConsole extends Console {

	public ExtrasConsole() {
		ExtrasComponent component = DaggerExtrasConsole_ExtrasComponent.builder()
				.extrasModule(new ExtrasModule())
				.build();
		pinto = component.pinto();
	}

	public static void main(String[] args) throws IOException {
		try {
			new ExtrasConsole().run();
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
		Cache provideCache(Vocabulary vocabulary) {
			return new LocalCache(vocabulary);
		}

		@Provides
		@Singleton
		Vocabulary provideVocabulary() {
			return new ExtraVocabulary();
		}
	}

	@Component(modules = ExtrasModule.class)
	@Singleton
	public interface ExtrasComponent {
		Pinto pinto();
	}

}
