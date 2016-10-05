package tech.pinto.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionHelp {
	
	final String name;
	final String inputs;
	final String outputs;
	final String description;
	final List<Parameter> parameters;

	private FunctionHelp(Builder b) {
		this.name = b.name;
		this.inputs = b.inputs;
		this.outputs = b.outputs;
		this.description = b.description;
		this.parameters  = b.parameters;
	}
	
	public String toTableRowString() {
		StringBuilder sb = new StringBuilder();
	//	sb.append("*").append(inputs).append("*|")
		sb.append("**").append(name);
		if(parameters.size() > 0) {
			sb.append(parameters.stream().map(p -> p.name).collect(Collectors.joining("*,*", "(*", "*)")));
		}
		sb.append("**|*").append(outputs).append("*|").append(description).append(" ");
		Map<String,String> defs = new HashMap<>();
		for(Parameter p : parameters) {
			if(p.defaultValue != null) {
				defs.put(p.name, p.defaultValue);
			}
		}
		if(defs.size() > 0) {
			sb.append(defs.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining("*,*", "(defaults: *", "*)")));
		}
		return sb.toString();
	}

	public String toConsoleHelpString() {
		String crlf = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
	//	sb.append("*").append(inputs).append("*|")
		sb.append(name).append(crlf);
		sb.append("\t").append(description).append(crlf);
		if(parameters.size() > 0) {
			sb.append(parameters.stream().map(p -> p.name).collect(Collectors.joining(", ", "\tParameters: ", crlf)));
		}
		Map<String,String> defs = new HashMap<>();
		for(Parameter p : parameters) {
			if(p.defaultValue != null) {
				defs.put(p.name, p.defaultValue);
			}
		}
		if(defs.size() > 0) {
			sb.append(defs.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining(", ", "\t(defaults: ", ")" + crlf)));
		}
		sb.append("\tOutput count for n inputs: ").append(outputs).append(crlf);
		return sb.toString();
	}
	
	public static class Builder {
		
		final String name;
		String inputs = "";
		String outputs = "";
		String description = "";
		List<Parameter> parameters = new ArrayList<>();
		
		public Builder(String name) {
			this.name = name;
		}
		
//		public Builder inputs(String inputs) {
//			this.inputs = inputs;
//			return this;
//		}

		public Builder outputs(String outputs) {
			this.outputs = outputs;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder parameter(String name) {
			return parameter(name,null,null);
		}

		public Builder parameter(String name, String defaultValue, String format) {
			parameters.add(new Parameter(name, defaultValue, format));
			return this;
		}
		
		public FunctionHelp build() {
			return new FunctionHelp(this);
		}
				
	}
	
	public static class Parameter {
		final String name;
		String defaultValue;
		String format;

		public Parameter(String name, String defaultValue, String format) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.format = format;
		}
		
	}

}
