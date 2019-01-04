package tech.pinto;



import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.util.Properties;

import javax.inject.Singleton;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Slf4jLog;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import jline.TerminalFactory;
import tech.pinto.DaggerMain_PintoComponent;
import tech.pinto.DaggerMain_MainComponent;
import tech.pinto.tools.NonSingletonScope;

public class Main {

	protected final PintoComponent component;
	protected String build;
	protected Server server;

	protected Main() {
		component = DaggerMain_PintoComponent.builder()
						.mainComponent(DaggerMain_MainComponent.builder().mainModule(new MainModule()).build())
						.build();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		try(InputStream resourceStream = loader.getResourceAsStream("pinto.properties")) {
		    props.load(resourceStream);
		    build = props.getProperty("buildTimestamp");
		} catch(IOException io) {
			build = "Unknown";
		}
//        System.out.println("public: " + getClass().getClassLoader().getResource("public").toExternalForm());

	}
	
	protected Pinto getPinto() {
		return component.pinto();
	}

	public void run() throws Exception {
		int port = System.getProperties().containsKey("pinto.port") ? Integer.parseInt(System.getProperty("pinto.port")) : 5556;
		try {
			server = getServer(port);
			server.start();
		} catch(BindException be) {
			server = getServer(0);
			server.start();
			port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
		}
		
		getPinto().setPort(port);

		org.eclipse.jetty.util.log.Log.setLog(new Slf4jLog());
		new Thread(new Console(getPinto(),port,build, () -> {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}), "console_thread").start();
        server.join();
	}
	
	private Server getServer(int port) {
		Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase(getClass().getClassLoader().getResource("public").toExternalForm());
        SessionHandler sessions = new SessionHandler();
        sessions.setHandler(context);
        context.addServlet(new ServletHolder(new Servlet(this::getPinto)),"/pinto/*");
        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(holderPwd,"/*");
        server.setHandler(sessions);
		return server;
	}

	public static void main(String[] args) throws IOException {
		try {
			new Main().run();
		} catch (Exception e) {
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
	public static class MainModule {
		@Provides Vocabulary vocabulary() {
			return new StandardVocabulary();
		}

		@Provides MarketData marketdata() {
			return new IEXMarketData();
		}
		@Provides Namespace provideNamespace(Vocabulary vocabulary) {
			return new Namespace(vocabulary);
		}
	}

	@NonSingletonScope
	@Component(dependencies = {MainComponent.class})
	public interface PintoComponent {
		Pinto pinto();
	}

	@Singleton
	@Component(modules = MainModule.class)
	public interface MainComponent {
		Namespace namespace();
		Vocabulary vocabulary();
		MarketData marketdata();
	}

}
