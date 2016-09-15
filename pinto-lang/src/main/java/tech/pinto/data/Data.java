package tech.pinto.data;

import tech.pinto.time.PeriodicRange;

public class Data<D> implements Comparable<Data<D>> {
	
	protected PeriodicRange<?> range;
	protected String label;
	protected D data;

	public Data(PeriodicRange<?> range, String label, D data) {
		this.range = range;
		this.label = label;
		this.data = data;
	}

	public String getLabel() {
		return label;
	}

	public Data<D> setLabel(String label) {
		this.label = label;
		return this;
	}

	public D getData() {
		return data;
	}
	
	public PeriodicRange<?> getRange() {
		return range;
	}

	public Data<D> setData(D data) {
		this.data = data;
		return this;
	}

	@Override
	public int compareTo(Data<D> o) {
		return range.start().compareTo(o.getRange().start());
	}
	
	

}
