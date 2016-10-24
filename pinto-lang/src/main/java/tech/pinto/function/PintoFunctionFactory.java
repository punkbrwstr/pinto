package tech.pinto.function;

import java.util.LinkedList;
import java.util.List;

import tech.pinto.Namespace;

public class PintoFunctionFactory implements FunctionFactory {

	private final Function function;
	
	public PintoFunctionFactory(Function function) {
		this.function = function;
	}

	@Override
	public Function build(String name, Namespace namespace, LinkedList<Function> inputs, List<String> saveString,
			String... arguments) {
		Function clone = function.clone();
		LinkedList<PlaceholderFunction> placeholders = new LinkedList<>();
		clone.getPlaceholders(placeholders);
		for(PlaceholderFunction ph : placeholders) {
			if(inputs.isEmpty()) {
				throw ph.getError();
			}
			ph.setDelagate(inputs.removeFirst());
		}
		if(placeholders.size() == 0) {
			LinkedList<Function> leaves = new LinkedList<>();
			clone.getLeafNodes(leaves);
			Function lastLeaf = leaves.getFirst();
			inputs.stream().forEach(lastLeaf.inputStack::addFirst);
		} else {
			inputs.stream().forEach(clone.inputStack::addFirst);
		}
		return clone;
	}

}
