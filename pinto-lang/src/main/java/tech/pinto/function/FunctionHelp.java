package tech.pinto.function;

import java.text.MessageFormat;
import java.util.Optional;

import tech.pinto.Parameters;

public class FunctionHelp {
	
	final String name;
	final String outputs;
	final String description;
	final Optional<Parameters> parameters;

	private FunctionHelp(Builder b) {
		this.outputs = b.outputs;
		this.description = b.description;
		this.parameters  = b.parameters;
		this.name = b.name;
	}
	
	public String toTableRowString() {
		StringBuilder sb = new StringBuilder();
		sb.append("**").append(name);
		sb.append("**|*").append(outputs).append("*|").append(description).append(" ");
		if(parameters.isPresent()) {
			sb.append(parameters.get().indexString());
			for(int i = 0; i < parameters.get().getNames().length; i++) {
				sb.append(parameters.get().getNames()[i]);
				if(parameters.get().getDescriptions()[i] != null ||
						parameters.get().getDefaults()[i] != null) {
					sb.append(" (");
					if(parameters.get().getDescriptions()[i] != null) {
							sb.append(parameters.get().getDescriptions()[i]);
					}
					if(parameters.get().getDefaults()[i] != null) {
							sb.append(" default: ").append(parameters.get().getDefaults()[i]);
					}
					sb.append(")");
				}
				if(i < parameters.get().getNames().length -1) {
					sb.append(", ");
				}
			}
		}
		return sb.toString();
	}

	public String toConsoleHelpString() {
		String crlf = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(crlf);
		sb.append("\t").append(description).append(crlf);
		if(parameters.isPresent()) {
			sb.append("\t").append("Parameters:").append(crlf);
			sb.append(parameters.get().indexString());
			for(int i = 0; i < parameters.get().getNames().length; i++) {
				sb.append("\t\t").append(parameters.get().getNames()[i]);
				if(parameters.get().getDescriptions()[i] != null ||
						parameters.get().getDefaults()[i] != null) {
					sb.append(" (");
					if(parameters.get().getDescriptions()[i] != null) {
							sb.append(parameters.get().getDescriptions()[i]);
					}
					if(parameters.get().getDefaults()[i] != null) {
							sb.append(" default: ").append(parameters.get().getDefaults()[i]);
					}
					sb.append(")");
				}
				sb.append(crlf);
			}
		}
		if(!outputs.equals("")) {
			sb.append("\tOutput count for n inputs: ").append(outputs).append(crlf);
		}
		return sb.toString();
	}
	
	public static class Builder {
		
		String name;
		String outputs = "none";
		String description = "";
		Optional<Parameters> parameters = Optional.empty();
		
		public Builder() {
		}
		
		public Builder outputs(String outputs) {
			this.outputs = outputs;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public Builder formatDescription(String... formatInputs) {
			MessageFormat mf = new MessageFormat(this.description);
			this.description = mf.format(formatInputs);
			return this;
		}

		public Builder parameters(Parameters parameters) {
			this.parameters = Optional.of(parameters);
			return this;
		}

		public FunctionHelp build(String name) {
			this.name = name;
			return new FunctionHelp(this);
		}
				
	}

}
