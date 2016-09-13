package pinto;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import pinto.command.CommandFactory;
import pinto.command.anyany.Duplicate;
import pinto.command.anyany.Label;
import pinto.command.anyany.Reverse;
import pinto.command.doubledouble.DoubleDoubleOperator;
import pinto.command.doubledouble.DoubleOperator;
import pinto.command.nonedouble.MoonPhase;
import pinto.command.nonedouble.Yahoo;
import pinto.command.terminal.Delete;
import pinto.command.terminal.Evaluate;
import pinto.command.terminal.Save;

public class PintoVocabulary implements Vocabulary {
    
    private final Map<String,CommandFactory> commands = 
            new ImmutableMap.Builder<String, CommandFactory>()
                .put("eval", (c,a) -> new Evaluate(a))
                .put("save", (c,a) -> new Save(c,a))
                .put("del", (c,a) -> new Delete(c,a))
                .put("+", (c,a) -> new DoubleDoubleOperator("+", (x,y) -> x + y))
                .put("-", (c,a) -> new DoubleDoubleOperator("-", (x,y) -> x - y))
                .put("*", (c,a) -> new DoubleDoubleOperator("*", (x,y) -> x * y))
                .put("/", (c,a) -> new DoubleDoubleOperator("/", (x,y) -> x / y))
                .put("%", (c,a) -> new DoubleDoubleOperator("%", (x,y) -> x % y))
                .put("==", (c,a) -> new DoubleDoubleOperator("==", (x,y) -> x == y ? 1.0 : 0.0))
                .put("!=", (c,a) -> new DoubleDoubleOperator("!=", (x,y) -> x != y ? 1.0 : 0.0))
                .put(">", (c,a) -> new DoubleDoubleOperator(">", (x,y) -> x > y ? 1.0 : 0.0))
                .put("<", (c,a) -> new DoubleDoubleOperator("<", (x,y) -> x < y ? 1.0 : 0.0))
                .put(">=", (c,a) -> new DoubleDoubleOperator(">=", (x,y) -> x >= y ? 1.0 : 0.0))
                .put("<=", (c,a) -> new DoubleDoubleOperator("<=", (x,y) -> x <= y ? 1.0 : 0.0))
                .put("abs", (c,a) -> new DoubleOperator("abs", x -> Math.abs(x)))
                .put("neg", (c,a) -> new DoubleOperator("neg", x -> x * -1d))
                .put("inv", (c,a) -> new DoubleOperator("inv", x -> 1.0 / x))
                .put("acgbConvert", (c,a) -> new DoubleOperator("acgbConvert",
                    quote -> {
                        double TERM = 10, RATE = 6, price = 0; 
                        for (int i = 0; i < TERM * 2; i++) {
                            price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
                        }
                        price += 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
                        return price; }))
                .put("moon", (c,a) -> new MoonPhase())
                .put("label", (c,a) -> new Label(a))
                .put("rev", (c,a) -> new Reverse())
                .put("dup", (c,a) -> new Duplicate())
                .put("yhoo", (c,a) -> new Yahoo(c,a))
                .build();

	@Override
	public Map<String, CommandFactory> getCommandMap() {
		return commands;
	}
    

}
