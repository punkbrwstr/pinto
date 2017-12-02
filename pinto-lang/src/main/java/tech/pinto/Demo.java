package tech.pinto;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


import org.eclipse.jetty.server.Server;
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
import tech.pinto.tools.LogAppender;

public class Demo {

	protected final DemoComponent component;
	protected String build;
	protected final int port;
	protected final String httpPath;

	protected Demo() {
		component = DaggerDemo_DemoComponent.builder().demoModule(new DemoModule()).build();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		try(InputStream resourceStream = loader.getResourceAsStream("pinto.properties")) {
		    props.load(resourceStream);
		    build = props.getProperty("buildTimestamp");
		} catch(IOException io) {
			build = "Unknown";
		}
		port = System.getProperties().containsKey("pinto.port") ? Integer.parseInt(System.getProperty("pinto.port")) : 5559;
		List<String> path = Arrays.asList(tech.pinto.Main.class.getResource("Main.class")
						.toString().split("/"));
		path = new ArrayList<>(path.subList(0, path.size()-3));
		path.add("public");
		httpPath = path.stream().collect(Collectors.joining(File.separator));
	}
	
	protected Pinto getPinto() {
		return component.pinto();
	}

	public void run() throws Exception {
		org.eclipse.jetty.util.log.Log.setLog(new Slf4jLog());
		Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{ "demo.html" });
        context.setResourceBase(httpPath);
        HashSessionIdManager idmanager = new HashSessionIdManager();
        server.setSessionIdManager(idmanager);
        HashSessionManager manager = new HashSessionManager();
        SessionHandler sessions = new SessionHandler(manager);
        sessions.setHandler(context);
        context.addServlet(new ServletHolder(new Servlet(this::getPinto)),"/pinto/*");
        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(holderPwd,"/*");
        server.setHandler(sessions);
        server.start();
        server.join();
	}

	public static void main(String[] args) throws IOException {
		try {
			new Thread(() -> {
				while(true) {
					try {
						System.out.println(LogAppender.LOG.take());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			},"log_printer").start();
			new Demo().run();
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

	public static class DemoVocabulary extends StandardVocabulary {
		
		public DemoVocabulary() {
			names.remove("exec");
			names.remove("exec");
			names.remove("exec");
		}
		
	}
	
	@Module
	public static class DemoModule {
		@Provides
		Vocabulary provideVocabulary() {
			return new DemoVocabulary();
		}
	}

	@Component(modules = DemoModule.class)
	public interface DemoComponent {
		Pinto pinto();
		Namespace namespace();
	}

}
