package tech.pinto;

import java.io.IOException;
	
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.inject.Singleton;


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
import tech.pinto.tools.Outputs;
import static spark.Spark.*;

public class Main {

	protected final MainComponent component;

	protected Main() {
		component = DaggerMain_MainComponent.builder().mainModule(new MainModule()).build();
		//Logger.getLogger().setLevel(Level.OFF);
	}
	
	protected Pinto getPinto() {
		return component.pinto();
	}

	public void run() throws IOException {
		new Thread(consoleRunner(),"console_thread").start();
		
		GsonBuilder b = new GsonBuilder();
		b.serializeSpecialFloatingPointValues();
		Gson gson = b.create();
		port(5556);
		staticFiles.location("/public");
		get("/pinto","application/json", (request, response) -> {
			try {
				Pinto pinto = getPinto();
				String statement = request.queryParams("statement");
				if(statement == null) {
					return new ImmutableMap.Builder<String,Object>()
							.put("responseType", "error")
							.put("exception", "empty statement").build();
				}
				boolean numbersAsString = request.queryParams().contains("numbers_as_string");
				boolean omitDates = request.queryParams().contains("omit_dates");
				List<Pinto.Response> output =  pinto.execute(statement);
				List<TimeSeries> data = output.stream().map(Pinto.Response::getTimeseriesOutput).filter(Optional::isPresent)
								.map(Optional::get).flatMap(List::stream).collect(Collectors.toList());
				List<String> messages = output.stream().map(Pinto.Response::getMessageOutput).filter(Optional::isPresent)
								.map(Optional::get).collect(Collectors.toList());
				if(data.size() == 0 && messages.size() == 0) {
					return new ImmutableMap.Builder<String,Object>()
							.put("responseType", "none").build();
				} else if(data.size() > 0) {
					return new ImmutableMap.Builder<String,Object>()
						.put("responseType", "data")
						.put("date_range", new ImmutableMap.Builder<String,String>()
								.put("start", data.get(0).getRange().start().endDate().toString())
								.put("end", data.get(0).getRange().end().endDate().toString())
								.put("freq", data.get(0).getRange().periodicity().code())
								.build()
							)
						.put("index", omitDates ? "" : data.get(0).getRange().dates().stream()
							.map(LocalDate::toString).collect(Collectors.toList()))
						.put("columns", data.stream().map(TimeSeries::getLabel).collect(Collectors.toList()))
						.put("data", numbersAsString ? 
								data.stream().map(TimeSeries::stream)
							.map(ds -> ds.mapToObj(Double::toString).collect(Collectors.toList())).collect(Collectors.toList())
								: data.stream().map(TimeSeries::stream)
							.map(DoubleStream::toArray).collect(Collectors.toList()))
						.build();
				} else {
					return new ImmutableMap.Builder<String,Object>()
						.put("responseType", "messages")
						.put("messages", messages)
						.build();
				}
			} catch(Throwable t) {
				t.printStackTrace();
				return new ImmutableMap.Builder<String,Object>()
					.put("responseType", "error")
					.put("exception", t).build();
			}
			
		}, gson::toJson);

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

				while ((line = reader.readLine()) != null) {
					try {
						List<Pinto.Response> output =  pinto.execute(line.toString());
						List<TimeSeries> data = output.stream().map(Pinto.Response::getTimeseriesOutput).filter(Optional::isPresent)
								.map(Optional::get).flatMap(List::stream).collect(Collectors.toList());
						if (data.size() > 0) {
							Optional<Outputs.StringTable> t = data.stream().collect(Outputs.doubleDataToStringTable());
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
		} catch (IOException e) {
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
