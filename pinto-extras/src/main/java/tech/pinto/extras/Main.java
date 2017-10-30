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
import tech.pinto.extras.functions.Bloomberg;
import tech.pinto.extras.functions.Futures;
import tech.pinto.extras.functions.terminal.Report;

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
			names.put("bbg", new Name((n,p,s,f,i) -> new Bloomberg(n,f,i,bc), Bloomberg.HELP_BUILDER));
			names.put("fut", new Name((n,p,s,f,i) -> new Futures(n,p,f,i), Futures.HELP_BUILDER));
			names.put("rpt_start", new Name((n,p,s,f,i) -> Report.getOpener(n,s,f,i), Report.OPENER_HELP_BUILDER));
			names.put("rpt_end", new Name((n,p,s,f,i) -> Report.getCloser(n,s,f,i), Report.CLOSER_HELP_BUILDER));
			names.put("rpt_chart", new Name((n,p,s,f,i) -> Report.getChart(n,s,f,i), Report.CHART_HELP_BUILDER));

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
