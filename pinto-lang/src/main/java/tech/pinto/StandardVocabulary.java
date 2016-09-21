package tech.pinto;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import tech.pinto.command.CommandFactory;
import tech.pinto.command.anyany.Duplicate;
import tech.pinto.command.anyany.Index;
import tech.pinto.command.anyany.Label;
import tech.pinto.command.anyany.Reverse;
import tech.pinto.command.anyany.Roll;
import tech.pinto.command.doubledouble.Cross;
import tech.pinto.command.doubledouble.DoubleCollectors;
import tech.pinto.command.doubledouble.DoubleDoubleOperator;
import tech.pinto.command.doubledouble.DoubleOperator;
import tech.pinto.command.doubledouble.Fill;
import tech.pinto.command.doubledouble.Rolling;
import tech.pinto.command.nonedouble.MoonPhase;
import tech.pinto.command.nonedouble.Yahoo;
import tech.pinto.command.terminal.Delete;
import tech.pinto.command.terminal.Evaluate;
import tech.pinto.command.terminal.Export;
import tech.pinto.command.terminal.Help;
import tech.pinto.command.terminal.Save;

public class StandardVocabulary implements Vocabulary {
    
    private final Map<String,CommandFactory> commands = 
            new ImmutableMap.Builder<String, CommandFactory>()
                .put("eval", (c,a) -> new Evaluate(a))
                .put("export", (c,a) -> new Export(a))
                .put("save", (c,a) -> new Save(c,a))
                .put("del", (c,a) -> new Delete(c,a))
                .put("help", (c,a) -> new Help(c,this,a))
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
                .put("index", (c,a) -> new Index(a))
                .put("rev", (c,a) -> new Reverse())
                .put("dup", (c,a) -> new Duplicate())
                .put("roll", (c,a) -> new Roll(a))
                .put("yhoo", (c,a) -> new Yahoo(c,a))
                .put("lag", (c,a) -> new Rolling("lag",DoubleCollectors.first,false, a))
	            .put("last", (c,a) -> new Rolling("last",DoubleCollectors.last, false, a))
	            .put("chg", (c,a) -> new Rolling("chg",DoubleCollectors.change,false, a))
	            .put("chg_pct", (c,a) -> new Rolling("chg_pct",DoubleCollectors.changepct,false, a))
	            .put("chg_log", (c,a) -> new Rolling("chg_log",DoubleCollectors.changelog,false, a))
	            .put("r_mean", (c,a) -> new Rolling("r_mean",DoubleCollectors.average, true, a))
	            .put("x_mean", (c,a) -> new Cross("x_mean",DoubleCollectors.average, a))
	            .put("r_max", (c,a) -> new Rolling("r_max",DoubleCollectors.max, true, a))
	            .put("x_max", (c,a) -> new Cross("x_max",DoubleCollectors.max, a))
	            .put("r_min", (c,a) -> new Rolling("r_max",DoubleCollectors.min, true, a))
	            .put("x_min", (c,a) -> new Cross("x_max",DoubleCollectors.min, a))
	            .put("r_sum", (c,a) -> new Rolling("r_sum",DoubleCollectors.sum, true, a))
	            .put("x_sum", (c,a) -> new Cross("x_sum",DoubleCollectors.sum, a))
	            .put("r_geomean", (c,a) -> new Rolling("r_geomean",DoubleCollectors.geomean, true, a))
	            .put("x_geomean", (c,a) -> new Cross("x_geomean",DoubleCollectors.geomean, a))
	            .put("r_var", (c,a) -> new Rolling("r_var",DoubleCollectors.var, true,a))
	            .put("x_var", (c,a) -> new Cross("x_var",DoubleCollectors.var,a))
	            .put("r_varp", (c,a) -> new Rolling("r_varp",DoubleCollectors.varp, true, a))
	            .put("x_varp", (c,a) -> new Cross("x_varp",DoubleCollectors.varp, a))
	            .put("r_std", (c,a) -> new Rolling("r_std",DoubleCollectors.stdev, true, a))
	            .put("x_std", (c,a) -> new Cross("x_std",DoubleCollectors.stdev, a))
	            .put("r_stdp", (c,a) -> new Rolling("r_stdp",DoubleCollectors.stdevp, true, a))
	            .put("x_stdp", (c,a) -> new Cross("x_stdp",DoubleCollectors.stdevp, a))
	            .put("r_zscore", (c,a) -> new Rolling("r_zscore",DoubleCollectors.zscore, true, a))
	            .put("x_zscore", (c,a) -> new Cross("x_zscore",DoubleCollectors.zscore, a))
	            .put("r_zscorep", (c,a) -> new Rolling("r_zscorep",DoubleCollectors.zscorep, true, a))
	            .put("x_zscorep", (c,a) -> new Cross("x_zscorep",DoubleCollectors.zscorep, a))
	            .put("fill", (c,a) -> new Fill(a))
                .build();
    public StandardVocabulary() {
    	
    }

	@Override
	public Map<String, CommandFactory> getCommandMap() {
		return commands;
	}
    

}
