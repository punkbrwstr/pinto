package tech.pinto.function.terminal;

import java.io.BufferedReader;




import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;


import tech.pinto.Expression;
import tech.pinto.Namespace;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.Function;
import tech.pinto.function.TerminalFunction;

public class Execute extends TerminalFunction {

	public Execute(String name, Namespace namespace, LinkedList<Function> inputs,  String... arguments) {
		super(name, inputs, arguments);
		if(arguments.length < 1) {
			throw new IllegalArgumentException("exec requires one argument.");
		}
		timeSeriesOutput = Optional.of(new ArrayList<>());
		try (BufferedReader reader = new BufferedReader(new FileReader(arguments[0]))) {
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	Expression s = new Expression(namespace, line, inputStack);
		    	if (s.getTerminalCommands().size() != 0) {
		    		while(!s.getTerminalCommands().isEmpty()) {
		    			TerminalFunction terminal = s.getTerminalCommands().removeLast();
		    			if(terminal.getTimeSeries().isPresent()) {
		    				timeSeriesOutput.get().addAll(terminal.getTimeSeries().get());
		    			}
		    			if(terminal.getText().isPresent()) {
		    				if(!message.isPresent()) {
		    					message = Optional.of("");
		    				}
		    				message = Optional.of(message.get() + "\n" + terminal.getText().get()); 
		    			}
		    		}
		    	}
		    	inputStack.addAll(s.getStack());
		    }
			
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cannot find pinto file \"" + arguments[0] + "\" to execute.");
		} catch (IOException e1) {
			throw new IllegalArgumentException("IO error for pinto file \"" + arguments[0] + "\" in execute.");
		} catch (PintoSyntaxException e) {
			throw new IllegalArgumentException("Pinto syntax error in  file \"" + arguments[0] + "\".", e);
		}
	}
	
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("varies")
				.description("Executes pinto program defined in *filename*.")
				.parameter("filename")
				.build();
	}
	
	
	

}
