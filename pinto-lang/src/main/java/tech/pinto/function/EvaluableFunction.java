package tech.pinto.function;


import java.util.stream.DoubleStream;

import tech.pinto.TimeSeries;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

final public class EvaluableFunction implements Cloneable {

	private java.util.function.Function<EvaluableFunction[],String> labeller;
	final private java.util.function.Function<EvaluableFunction[],
			java.util.function.Function<PeriodicRange<?>,DoubleStream>> evaluationFunction;
	private EvaluableFunction[] inputs;

	public EvaluableFunction(java.util.function.Function<EvaluableFunction[],String> labeller,
			java.util.function.Function<EvaluableFunction[],
				java.util.function.Function<PeriodicRange<?>,DoubleStream>> evaluationFunction,
				EvaluableFunction...inputs) {
		this.labeller = labeller;
		this.evaluationFunction = evaluationFunction;
		this.inputs = inputs;
	}

	public <P extends Period> TimeSeries evaluate(PeriodicRange<P> range) {
		return new TimeSeries(range,toString(),evaluationFunction.apply(inputs).apply(range));
	}
	
	public void setLabeller(java.util.function.Function<EvaluableFunction[], String> labeller) {
		this.labeller = labeller;
	}

	@Override
	public String toString() {
		return labeller.apply(inputs);
	}

	@Override
	public EvaluableFunction clone() {
		EvaluableFunction clone;
		try {
			clone = (EvaluableFunction) super.clone();
			clone.inputs = new EvaluableFunction[inputs.length];
			for(int i = 0; i < inputs.length; i++) {
				clone.inputs[i] = (EvaluableFunction) inputs[i].clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
	}
	
	
	

}
