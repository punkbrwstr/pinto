package tech.pinto;



import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.util.Properties;

import javax.inject.Singleton;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Slf4jLog;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import jline.TerminalFactory;
import tech.pinto.tools.NonSingletonScope;

public class Main {

	protected final PintoComponent component;
	protected String build;
	protected Server server;

	protected Main() {
		//component = DaggerMain_MainComponent.builder().mainModule(new MainModule()).build();
		component = DaggerMain_PintoComponent.builder()
					.namespaceComponent(DaggerMain_NamespaceComponent.builder()
						.vocabularyComponent(DaggerMain_VocabularyComponent.builder()
								.vocabularyModule(new VocabularyModule()).build()).build()).build();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		try(InputStream resourceStream = loader.getResourceAsStream("pinto.properties")) {
		    props.load(resourceStream);
		    build = props.getProperty("buildTimestamp");
		} catch(IOException io) {
			build = "Unknown";
		}


        System.out.println("public: " + getClass().getClassLoader().getResource("public").toExternalForm());

		//List<String> path = Arrays.asList(tech.pinto.Main.class.getResource("Main.class")
						//.toString().split("/"));
		//path = new ArrayList<>(path.subList(0, path.size()-3));
		//path.add("public");
		//httpPath = path.stream().collect(Collectors.joining(File.separator));
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
        HashSessionIdManager idmanager = new HashSessionIdManager();
        server.setSessionIdManager(idmanager);
        HashSessionManager manager = new HashSessionManager();
        SessionHandler sessions = new SessionHandler(manager);
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
	public static class VocabularyModule {
		@Provides Vocabulary vocabulary() {
			return new StandardVocabulary();
		}
	}

	@NonSingletonScope
	@Component(dependencies = NamespaceComponent.class)
	public interface PintoComponent {
		Pinto pinto();
	}

	@Singleton
	@Component(dependencies = VocabularyComponent.class)
	public interface NamespaceComponent {
		Namespace namespace();
	}

	@Component(modules = VocabularyModule.class)
	public interface VocabularyComponent {
		Vocabulary vocabulary();
	}

}
