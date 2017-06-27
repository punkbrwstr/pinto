package tech.pinto.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SearchableMultiMap<V> extends HashMap<String,List<V>> {

	private static final long serialVersionUID = 1L;
	
	private TreeSet<String> forwardIndex = new TreeSet<>();
	private TreeSet<String> backwardIndex = new TreeSet<>();

	public SearchableMultiMap() {
		super();
	}

	public void add(String key, V value) {
		if(!this.containsKey(key)) {
			this.put(key, new ArrayList<>());
			forwardIndex.add(key);
			backwardIndex.add(new StringBuilder(key).reverse().toString());
		}
		this.get(key).add(value);
	}
	
	public Optional<List<V>> search(String query) {
		Optional<List<V>> results = Optional.empty();
		Set<String> keys = new TreeSet<>();
		if((!query.contains("*") && this.containsKey(query))) {
			keys.add(query);
		} else if(query.equals("*")) {
			keys.addAll(forwardIndex);
		} else {
			String[] parts = query.split("\\*");
			if(parts.length > 2) {
				throw new IllegalArgumentException("Search only supports a single wildcard.");
			}
			String frontPart = parts[0];
			String backPart = parts.length == 2 ? parts[1] : "";
			backPart = new StringBuilder(backPart).reverse().toString();
			if(!frontPart.equals("")) {
				for(String key : forwardIndex.tailSet(frontPart)) {
					if(key.startsWith(frontPart)) {
						keys.add(key);
					} else {
						break;
					}
				}
			}
			if(!backPart.equals("")) {
				HashSet<String> backKeys = new HashSet<>();
				for(String key : backwardIndex.tailSet(backPart)) {
					if(key.startsWith(backPart)) {
						backKeys.add(new StringBuilder(key).reverse().toString());
					} else {
						break;
					}
				}
				if(frontPart.equals("")) {
					keys.addAll(backKeys);
				} else {
					keys.retainAll(backKeys);
				}
			}

		}
		for(String key : keys) {
			if(!results.isPresent()) {
				results = Optional.of(new ArrayList<>(this.get(key)));
			} else {
				results.get().addAll(this.get(key));
			}
		}
		return results;
	}

	
	public static void main(String args[]) {
		SearchableMultiMap<Integer> test = new SearchableMultiMap<>();
		test.add("hotdog", 1);
		test.add("hotdog", 50);
		test.add("doghouse", 2);
		test.add("hotdiggittydog", 3);
		test.add("doghouseboat", 4);
		test.add("chicagohotdog", 5);
		
		for(String tester : Arrays.asList("*","hotdog","hamburger","hotdog*","doghouse*",
											"*hotdog","hot*dog", "*burger", "*")) {
			System.out.print("Testing " + tester + ": ");
			System.out.print(test.search(tester).orElse(Arrays.asList(-1)).stream().map(o -> o.toString()).collect(Collectors.joining(",")));
			System.out.println();
		}
		
	}

}
