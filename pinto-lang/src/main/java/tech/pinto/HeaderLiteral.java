package tech.pinto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class HeaderLiteral implements Consumer<LinkedList<Column<?,?>>>{
	
	private final List<String[]> headers;
	private final Pinto pinto;
	
	public HeaderLiteral(Pinto pinto, String header) {
		this.headers = new ArrayList<>();

		String[] as = new String[2];
		int start = 0;
		boolean foundColon = false, foundQuote = false;
		String raw = header.replaceAll("^\\{|\\}$", "");
		for(int i = 0; i < raw.length(); i++) {
			if(raw.charAt(i) == '"') {
				foundQuote = !foundQuote;
			}
			if(raw.charAt(i) == ':') {
				if(foundColon) {
					throw new PintoSyntaxException("Repeated \":\" in header literal");
				} else if(foundQuote) {
					throw new PintoSyntaxException("Double-quote found in header literal");
				}
				as[0] = raw.substring(start, i);
				start = i+1;
				foundColon = true;
			} else if(raw.charAt(i) == ',' || i == raw.length()-1) {
				if(!foundQuote) {
					if(!foundColon) {
						as[0] = raw.substring(start, raw.charAt(i) == ',' ? i : i + 1);
					} else {
						as[1] = raw.substring(start, raw.charAt(i) == ',' ? i : i + 1);
						foundColon = false;
					}
					this.headers.add(as);
					as = new String[2];
					start = i+1;
				}
			}
		}
		//this.headers = Stream.of(header.replaceAll("\\{|\\}", "").split(","))
							//.map(s -> s.split(":")).collect(Collectors.toList());
		this.pinto = pinto;
	}

	@Override
	public void accept(LinkedList<Column<?, ?>> stack) {
		LinkedList<Column<?,?>> newStack = new LinkedList<>();
		for(int i = headers.size() - 1; i >= 0; i--) {
			final String label = headers.get(i)[0].trim();
			LinkedList<Column<?,?>> unlabeled = new LinkedList<>();
			if(headers.get(i)[1] == null) {
				if(!stack.isEmpty()) {
					unlabeled.addLast(stack.removeFirst());
				}
			} else {
				unlabeled.addAll(pinto.parseSubExpression(headers.get(i)[1]));
			}
			for(Column<?,?> c : unlabeled) {
				c.setHeaderFunction(inputs -> label);
				newStack.addLast(c);
			}
		}
		stack.addAll(0, newStack);
	}

}
