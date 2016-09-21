package tech.pinto.command.anyany;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Functions;

import tech.pinto.command.Command;
import tech.pinto.command.ParameterizedCommand;
import tech.pinto.command.SimpleCommand;
import tech.pinto.data.AnyData;
import tech.pinto.data.Data;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Index extends ParameterizedCommand {
	
	private List<Command> commandList;
	private HashMap<String,Data<?>> dataMap;
	private int refCount = 0;

	public Index(String[] arguments) {
		super("index", AnyData.class, AnyData.class, arguments);
		inputCount = Integer.MAX_VALUE;
		outputCount = arguments.length;
	}
	

	@Override public Command getReference() {
		if(commandList == null) {
			commandList = inputStack.stream().collect(Collectors.toList());
		}
		String arg = arguments[refCount++].trim();
		if(isInteger(arg)) {
			return commandList.get(Integer.parseInt(arg)).clone();
		} else {
			return new SimpleCommand("index",AnyData.class,AnyData.class,1,1,range -> {
				return this.evaluateForLabel(arg, range);
		});
			
		}
	}

	public <P extends Period> Data<?> evaluateForLabel(String label, PeriodicRange<P> range) {
		if(dataMap == null) {
			dataMap = new HashMap<>();
			for(Data<?> d : inputStack.stream().map(c -> c.evaluate(range)).collect(Collectors.toList())){
				dataMap.put(d.getLabel(), d);
			}
		}
		if(!dataMap.containsKey(label)) {
			throw new IllegalArgumentException("invalid index label key: \"" + label + "\"");
		}
		return dataMap.get(label);
	}

	@Override
	public <P extends Period> Data<?> evaluate(PeriodicRange<P> range) {
		// never gets called bc it passes on references to inputs
		throw new UnsupportedOperationException();
	}
	
	private boolean isInteger(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}

}
