package tech.pinto.extras;


import java.io.IOException;



import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import jline.TerminalFactory;
import tech.pinto.Name;
import tech.pinto.Namespace;
import tech.pinto.Pinto;
import tech.pinto.StandardVocabulary;
import tech.pinto.Vocabulary;
import tech.pinto.extras.function.supplier.Bloomberg;
import tech.pinto.extras.function.supplier.Futures;
import tech.pinto.function.FunctionHelp;

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

	public static class ExtraVocabulary extends StandardVocabulary {
		public BloombergClient bc = new BloombergClient();
		
		public ExtraVocabulary() {
			names.put("bbg", new Name((n,c,i,s,a) -> new Bloomberg(n,bc,i,a), n -> new FunctionHelp.Builder(n).build()));
			names.put("fut", new Name((n,c,i,s,a) -> new Futures(n,c,i,a), n -> new FunctionHelp.Builder(n).build()));

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
	}

	@Component(modules = ExtrasModule.class)
	@Singleton
	public interface ExtrasComponent {
		Pinto pinto();
	}

}
