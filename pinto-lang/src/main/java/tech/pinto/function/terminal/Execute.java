package tech.pinto.function.terminal;

import java.io.BufferedReader;





import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Pinto;
import tech.pinto.PintoSyntaxException;
import tech.pinto.TimeSeries;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;

public class Execute extends TerminalFunction {

	private LinkedList<TimeSeries> timeSeriesOutput = null;
	private final List<String> stringOutput = new ArrayList<>();
	private final Pinto pinto;
	
	public Execute(String name, Pinto pinto, Namespace namespace, ComposableFunction previousFunction, Indexer indexer, String... args) {
		super(name, namespace, previousFunction, indexer, args);
		this.pinto = pinto;
	}
	
	private void executeFile() {
		if(args.length < 1) {
			throw new IllegalArgumentException("exec requires one argument.");
		}
		timeSeriesOutput = new LinkedList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	for(TerminalFunction tf : pinto.execute(line)) {
		    		tf.getText().ifPresent(stringOutput::add);
		    		tf.getTimeSeries().ifPresent(timeSeriesOutput::addAll);
		    	}
		    }
			
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cannot find pinto file \"" + args[0] + "\" to execute.");
		} catch (IOException e1) {
			throw new IllegalArgumentException("IO error for pinto file \"" + args[0] + "\" in execute.");
		} catch (PintoSyntaxException e) {
			throw new IllegalArgumentException("Pinto syntax error in  file \"" + args[0] + "\".", e);
		} catch (Exception e) {
			throw new IllegalArgumentException("Pinto syntax error in  file \"" + args[0] + "\".", e);
		}	
	}

	@Override
	public Optional<LinkedList<TimeSeries>> getTimeSeries() throws PintoSyntaxException {
		if(timeSeriesOutput == null) {
			executeFile();
		}
		return timeSeriesOutput.size() == 0 ? Optional.empty() : Optional.of(timeSeriesOutput);
	}
	
	
	
	@Override
	public Optional<String> getText() throws PintoSyntaxException {
		if(timeSeriesOutput == null) {
			executeFile();
		}
		return stringOutput.size() == 0 ? Optional.empty() : Optional.of(stringOutput.stream().collect(Collectors.joining("\n")));
	}

	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("varies")
				.description("Executes pinto program defined in *filename*.")
				.parameter("filename")
				.build();
	}
	
	
	

}
