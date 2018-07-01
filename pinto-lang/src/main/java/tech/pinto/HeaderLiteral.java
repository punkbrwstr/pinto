package tech.pinto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class HeaderLiteral implements Consumer<LinkedList<Column<?>>>{
	
	private final List<String[]> headers;
	private final Pinto pinto;
	
	public HeaderLiteral(Pinto pinto, String header) {
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
					this.headers.add(new String[] {h[0].toString(),h[1].length() == 0 ? null : h[1].toString()});
					h = new StringBuilder[] {new StringBuilder(), new StringBuilder()};
					Arrays.setAll(open, x -> 0);
					position = 0;
				} else {
					 h[position].append(header.charAt(i));
				}
			}
		}
		if(Arrays.stream(open).sum() == 0) { 
			this.headers.add(new String[] {h[0].toString(),h[1].length() == 0 ? null : h[1].toString()});
		} else {
			String unmatched = IntStream.range(0, 4).mapToObj(i -> open[i] == 0 ? "" : new String[]{"\"","$","{","["}[i])
					.filter(s -> !s.equals("")).collect(Collectors.joining(","));
			throw new IllegalArgumentException("Unmatched \"" + unmatched + "\" in header literal: \"[" + header + "]\"");
		}
		this.pinto = pinto;
	}

	@Override
	public void accept(LinkedList<Column<?>> stack) {
		LinkedList<Column<?>> newStack = new LinkedList<>();
		for(int i = headers.size() - 1; i >= 0; i--) {
			final String label = headers.get(i)[0].trim();
			LinkedList<Column<?>> unlabeled = new LinkedList<>();
			if(headers.get(i)[1] == null) {
				if(!stack.isEmpty()) {
					unlabeled.addLast(stack.removeFirst());
				}
			} else {
				unlabeled.addAll(pinto.parseSubExpression(headers.get(i)[1]));
			}
			for(Column<?> c : unlabeled) {
				c.setHeader(label);
				newStack.addLast(c);
			}
		}
		stack.addAll(0, newStack);
	}

}
