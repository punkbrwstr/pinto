package tech.pinto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Slf4jLog;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.fliptables.FlipTable;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import tech.pinto.tools.LogAppender;
import tech.pinto.tools.Outputs;

public class Main {

	protected final MainComponent component;
	protected String build;
	protected final int port;
	protected final String httpPath;

	protected Main() {
		component = DaggerMain_MainComponent.builder().mainModule(new MainModule()).build();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		try(InputStream resourceStream = loader.getResourceAsStream("pinto.properties")) {
		    props.load(resourceStream);
		    build = props.getProperty("buildTimestamp");
		} catch(IOException io) {
			build = "Unknown";
		}
		port = System.getProperties().containsKey("pinto.port") ? Integer.parseInt(System.getProperty("pinto.port")) : 5556;
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
		new Thread(consoleRunner(), "console_thread").start();

		Slf4jLog logger = new Slf4jLog();
		Pinto pinto = getPinto();
		org.eclipse.jetty.util.log.Log.setLog(logger);
		GsonBuilder b = new GsonBuilder();
		b.serializeSpecialFloatingPointValues();
		Gson gson = b.create();
		Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(new HttpServlet(){
        	private static final long serialVersionUID = 1L;
        	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        		response.setContentType("application/json");
				String statement = request.getParameter("statement");
				try {
					if (statement == null) {
						writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "error")
								.put("exception", "empty statement").build());
					}
					boolean numbersAsString = request.getParameterMap().containsKey("numbers_as_string");
					boolean omitDates = request.getParameterMap().containsKey("omit_dates");
					List<Pinto.Response> output = pinto.execute(statement);
					List<TimeSeries> data = output.stream().map(Pinto.Response::getTimeseriesOutput)
							.filter(Optional::isPresent).map(Optional::get).flatMap(List::stream)
							.collect(Collectors.toList());
					List<String> messages = output.stream().map(Pinto.Response::getMessageOutput)
							.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
					if (data.size() == 0 && messages.size() == 0) {
						writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "none").build());
					} else if (data.size() > 0) {
						writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "data")
								.put("date_range",
										new ImmutableMap.Builder<String, String>()
												.put("start", data.get(0).getRange().start().endDate().toString())
												.put("end", data.get(0).getRange().end().endDate().toString())
												.put("freq", data.get(0).getRange().periodicity().code()).build())
								.put("index",
										omitDates ? ""
												: data.get(0).getRange().dates().stream().map(LocalDate::toString)
														.collect(Collectors.toList()))
								.put("columns", data.stream().map(TimeSeries::getLabel).collect(Collectors.toList()))
								.put("data",
										numbersAsString
												? data.stream().map(TimeSeries::stream)
														.map(ds -> ds.mapToObj(Double::toString)
																.collect(Collectors.toList()))
														.collect(Collectors.toList())
												: data.stream().map(TimeSeries::stream).map(DoubleStream::toArray)
														.collect(Collectors.toList()))
								.build());
					} else {
						writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "messages")
								.put("messages", messages).build());
					}
				} catch (Throwable t) {
					t.printStackTrace();
					writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "error").put("exception", t)
							.build());
				}
        	}
        	
        	protected void writeResponse(HttpServletResponse response, Object o) throws IOException {
        		response.getOutputStream().print(gson.toJson(o));
        	}
        }),"/pinto");
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(httpPath);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, servletHandler });
        server.setHandler(handlers);
        //context.addServlet(DefaultServlet.class, "/");
        server.start();
        server.join();
	}

	public Runnable consoleRunner() {
		return () -> {
			try {
				Pinto pinto = getPinto();
				ConsoleReader reader = new ConsoleReader();
				reader.setPrompt("pinto> ");
				reader.addCompleter(new StringsCompleter(pinto.getVocab().getCommandNames()));
				String line;
				PrintWriter out = new PrintWriter(reader.getOutput());
				out.println("Pinto (build: " + build + ")");
				out.println("Server started on http://127.0.0.1:" + port);
				out.println("For help type \"help\".  To quit type \"\\q\". To show log type \"\\log\".");

				while ((line = reader.readLine()) != null) {
					if (line.indexOf("\\") == 0) {
						if(line.startsWith("\\q")) {
							break;
						} else if(line.startsWith("\\log")) {
							while(!LogAppender.LOG.isEmpty()) {
								try {
									System.out.println(LogAppender.LOG.take());
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					} else {
						try {
							List<Pinto.Response> output = pinto.execute(line.toString());
							List<TimeSeries> data = output.stream().map(Pinto.Response::getTimeseriesOutput)
									.filter(Optional::isPresent).map(Optional::get).flatMap(List::stream)
									.collect(Collectors.toList());
							if (data.size() > 0) {
								Optional<Outputs.StringTable> t = data.stream()
										.collect(Outputs.doubleDataToStringTable());
								if (t.isPresent()) {
									out.println(FlipTable.of(t.get().getHeader(), t.get().getCells()));
								}
							}
							output.stream().map(Pinto.Response::getMessageOutput).filter(Optional::isPresent)
									.map(Optional::get).forEach(out::println);
						} catch (PintoSyntaxException pse) {
							System.out.println("Incorrect syntax: " + pse.getMessage());
							pse.printStackTrace();
						} catch (Throwable e) {
							System.out.println("Evaluation error");
							e.printStackTrace();
						}
					}
					out.flush();
				}
			} catch (IOException e) {
				System.out.println(e);

			}
		};
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
		@Provides
		@Singleton
		Cache provideCache(Vocabulary vocabulary) {
			return new LocalCache(vocabulary);
		}

		@Provides
		@Singleton
		Vocabulary provideVocabulary() {
			return new StandardVocabulary();
		}
	}

	@Component(modules = MainModule.class)
	@Singleton
	public interface MainComponent {
		Pinto pinto();
	}

}
