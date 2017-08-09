package tech.pinto.function.functions.terminal;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import tech.pinto.Indexer;
import tech.pinto.Namespace;
import tech.pinto.Parameters;
import tech.pinto.Pinto;
import tech.pinto.PintoSyntaxException;
import tech.pinto.Table;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.TerminalFunction;

public class Execute extends TerminalFunction {
	private static final Parameters.Builder PARAMETERS_BUILDER = new Parameters.Builder()
			.add("filename", true, "Path to .pinto file");
	public static final FunctionHelp.Builder HELP_BUILDER = new FunctionHelp.Builder()
			.parameters(PARAMETERS_BUILDER.build())
				.description("Executes pinto program defined in *filename*");

	private final Pinto pinto;
	
	public Execute(String name, Pinto pinto, Namespace namespace, ComposableFunction previousFunction, Indexer indexer) {
		super(name, namespace, previousFunction, indexer);
		this.pinto = pinto;
		this.parameters = Optional.of(PARAMETERS_BUILDER.build());
	}

	@Override
	public Table getTable() throws PintoSyntaxException {
		String filename = parameters.get().getArgument("filename");
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	pinto.execute(line);
		    }
			return createTextColumn("Executed.");
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cannot find pinto file \"" + filename + "\" to execute.");
		} catch (IOException e1) {
			throw new IllegalArgumentException("IO error for pinto file \"" + filename + "\" in execute.");
		} catch (PintoSyntaxException e) {
			throw new IllegalArgumentException("Pinto syntax error in  file \"" + filename + "\".", e);
		} catch (Exception e) {
			throw new IllegalArgumentException("Pinto syntax error in  file \"" + filename + "\".", e);
		}	
	}
}
