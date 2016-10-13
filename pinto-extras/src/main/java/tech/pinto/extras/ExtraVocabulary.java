package tech.pinto.extras;


import tech.pinto.StandardVocabulary;
import tech.pinto.extras.function.supplier.Bloomberg;
import tech.pinto.extras.function.supplier.Futures;

public class ExtraVocabulary extends StandardVocabulary {
	public BloombergClient bc = new BloombergClient();
	
	public ExtraVocabulary() {
		commands.put("bbg", (c,i,s,a) -> new Bloomberg(bc,c,i,a));
		commands.put("fut", (c,i,s,a) -> new Futures(c,this,i,a));

		commandHelp.putAll(new StandardVocabulary().getCommandHelpMap());
	}


}
