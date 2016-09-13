package tech.pinto.data;

import tech.pinto.time.PeriodicRange;

public class AnyData extends Data<Object> {

	public AnyData(PeriodicRange<?> range, String label, Object data) {
		super(range,label, data);
	}

	

}
