package tech.pinto.data;


public class MessageData extends Data<String> {

	public MessageData(String data) {
		super(null, null, data);
	}

	@Override
	public String toString() {
		return data;
	}
	
	
}
