package tech.pinto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Parameters {
	final String[] names; 
	final Boolean[] required;
	final String[] values;
	final String[] defaults;
	final String[] descriptions;
	final String indexString;
	
	protected Parameters(String[] names, Boolean[] required, String[] defaults, String[] descriptions) {
		this.names = names;
		this.required = required;
		this.defaults = defaults;
		this.descriptions = descriptions;
		values = new String[names.length];
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < names.length; i++) {
			sb.append(names[i]).append("=*|").append(i);
			if(!required[i]) {
				sb.append("?");
			}
			if(i < names.length -1) {
				sb.append(",");
			}
		}
		indexString = sb.toString();
	}
	
	public String[] getNames() {
		return names;
	}

	public Boolean[] getRequired() {
		return required;
	}

	public String[] getValues() {
		return values;
	}

	public String[] getDefaults() {
		return defaults;
	}

	public String[] getDescriptions() {
		return descriptions;
	}

	public String getArgument(String parameterName) {
		for(int i = 0; i < names.length; i++) {
			if(parameterName.equals(names[i])) {
				if(values[i] != null) {
					return values[i];
				} else {
					return defaults[i];
				}
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String name : getNames()) {
			String value = getArgument(name);
			if(value != null) {
				sb.append("\"").append(name).append("=").append(value).append("\"");
			}
		}
		return sb.toString();
	}

	public boolean hasArgument(String parameterName) {
		return getArgument(parameterName) != null;
	}
	
	public String indexString() {
		return indexString;
	}
	
	public void parseArguments(LinkedList<Column> stack) {
		LinkedList<Column> indexedStack = new Indexer(indexString).index(stack).get(0);
		int needed = names.length;
		while(!indexedStack.isEmpty() && indexedStack.getFirst().isHeaderOnly() && needed-- > 0) {
			for(String s : indexedStack.removeFirst().getHeader().split(";")) {
				if(s.contains("=")) {
					String[] sa = s.split("=");
					for(int i = 0; i < names.length; i++) {
						if(sa[0].trim().equals(names[i])) {
							values[i] = sa[1].trim();
							break;
						}
					}
				} else {
					for(int i = 0; i < names.length; i++) {
						if(values[i] == null) {
							values[i] = s.trim();
							break;
						}
					}
				}
			}
		}
		while(!indexedStack.isEmpty()) {
			stack.addFirst(indexedStack.removeLast());
		}
		for(int i = 0; i < names.length; i++) {
			if(values[i] == null & required[i]) {
				throw new IllegalArgumentException("Missing required argument for " + names[i]);
			}
		}
	}
	
	public static class Builder {
		List<String> names = new ArrayList<>();
		List<String> defaults = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();
		List<Boolean> required = new ArrayList<>();
		
		public Builder add(String name, boolean required, String description) {
			names.add(name);
			defaults.add(null);
			descriptions.add(description);
			this.required.add(required);
			return this;
		}

		public Builder add(String name, boolean required) {
			return add(name,required,null);
		}

		public Builder add(String name, String defaultValue) {
			return add(name,defaultValue,null);
		}

		public Builder add(String name, String defaultValue, String description) {
			names.add(name);
			defaults.add(defaultValue);
			descriptions.add(description);
			this.required.add(false);
			return this;
		}
		
		public Parameters build() {
			return new Parameters(names.toArray(new String[] {}), required.toArray(new Boolean[] {}),
									defaults.toArray(new String[] {}), descriptions.toArray(new String[] {}));
		}
	}
}
