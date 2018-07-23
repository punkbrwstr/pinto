package tech.pinto;

import java.util.ArrayList;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HeaderLiteral implements Consumer<LinkedList<Column<?>>> {
	
	private final List<Header> headers;
	
	public HeaderLiteral(Pinto pinto, Set<String> dependencies, String header) {
		this.headers = new ArrayList<>();

		StringBuilder[] h = new StringBuilder[] {new StringBuilder(), new StringBuilder()};
		int position = 0;
		final int[] open = new int[4]; // ", $, {, [

		for(int i = 0; i < header.length(); i++) {
			// first check what's open
			switch(header.charAt(i)) {
				case '"':	open[0] = open[0] == 0 ? 1 : 0;		break;
				case '$':	open[1] = open[1] == 0 ? 1 : 0;		break;
				case '{':	open[2]++;							break;
				case '}':	open[2]--;							break;
				case '[':	open[3]++;							break;
				case ']':	open[3]--;							break;
			}

			// don't count colons of commas if anything's open 
			if(Arrays.stream(open).sum() > 0) { 
				h[position].append(header.charAt(i));
			} else {
				if(header.charAt(i) == ':') {
					position = 1;
				} else if(header.charAt(i) == ',') {
					this.headers.add(new Header(pinto, dependencies,
							h[0].toString(),h[1].length() == 0 ? Optional.empty() : Optional.of(h[1].toString())));
					h = new StringBuilder[] {new StringBuilder(), new StringBuilder()};
					Arrays.setAll(open, x -> 0);
					position = 0;
				} else {
					 h[position].append(header.charAt(i));
				}
			}
		}
		if(Arrays.stream(open).sum() == 0) { 
			this.headers.add(new Header(pinto, dependencies,
					h[0].toString(),h[1].length() == 0 ? Optional.empty() : Optional.of(h[1].toString())));
		} else {
			String unmatched = IntStream.range(0, 4).mapToObj(i -> open[i] == 0 ? "" : new String[]{"\"","$","{","["}[i])
					.filter(s -> !s.equals("")).collect(Collectors.joining(","));
			throw new IllegalArgumentException("Unmatched \"" + unmatched + "\" in header literal: \"[" + header + "]\"");
		}
	}

	@Override
	public void accept(LinkedList<Column<?>> stack) {
		LinkedList<Column<?>> newStack = new LinkedList<>();
		for(int i = headers.size() - 1; i >= 0; i--) {
			if(!headers.get(i).getDefaultColumns().isPresent()) {
				if(!stack.isEmpty()) {
					Column<?> c = stack.removeFirst();
					c.setHeader(headers.get(i).getHeader());
					newStack.addLast(c);
				}
			} else {
				Table t = new Table();
				final String header = headers.get(i).getHeader();
				headers.get(i).getDefaultColumns().get().accept(t);
				t.flatten().stream().peek(c -> c.setHeader(header)).forEach(newStack::add);
			}
		}
		stack.addAll(0, newStack);
	}
	
	private static class Header {
		private final String header;
		private final Optional<Pinto.Expression> defaultColumns;
		public Header(Pinto pinto, Set<String> dependencies, String header, Optional<String> defaultColumnExpression) {
			this.header = header.trim();
			if(defaultColumnExpression.isPresent()) {
				Pinto.Expression e = pinto.parseSubExpression(defaultColumnExpression.get());
				dependencies.addAll(e.getDependencies());
				this.defaultColumns = Optional.of(e);
			} else {
				this.defaultColumns = Optional.empty();
			}

			
		}
		public String getHeader() {
			return header;
		}
		public Optional<Pinto.Expression> getDefaultColumns() {
			return defaultColumns;
		}
		
		
	}
}
