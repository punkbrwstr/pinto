package tech.pinto;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import tech.pinto.command.CommandFactory;
import tech.pinto.command.CommandHelp;
import tech.pinto.command.anyany.Copy;
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
import tech.pinto.command.doubledouble.RollingCorrelation;
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
                .put("def", (c,a) -> new Save(c,a))
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
                .put("copy", (c,a) -> new Copy(a))
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
	            .put("correl", (c,a) -> new RollingCorrelation(a))
                .build();

    private final Map<String,Supplier<CommandHelp>> commandHelp = 
            new ImmutableMap.Builder<String, Supplier<CommandHelp>>()
            /* terminal commands */
                .put("eval", Evaluate.getHelp())
                .put("export", Export.getHelp())
                .put("def", Save.getHelp())
                .put("help", Help.getHelp())
                .put("del", Delete.getHelp())

            /* stack manipulation commands */
                .put("label", Label.getHelp())
                .put("copy", Copy.getHelp())
                .put("roll", Roll.getHelp())
                .put("index", Index.getHelp())

            /* initial data commands */
                .put("yhoo", Yahoo.getHelp())
                .put("moon", MoonPhase.getHelp())

            /* rolling window commands */
                .put("chg",Rolling.getHelp("chg", "change"))
                .put("chg_pct",Rolling.getHelp("chg_pct", "change in percent"))
                .put("chg_log",Rolling.getHelp("chg_log", "log change"))
                .put("r_mean",Rolling.getHelp("r_mean", "mean"))
                .put("r_max",Rolling.getHelp("r_max", "maximum"))
                .put("r_min",Rolling.getHelp("r_min", "minimum"))
                .put("r_sum",Rolling.getHelp("r_sum", "sum"))
                .put("r_geomean",Rolling.getHelp("r_geomean", "geometric mean"))
                .put("r_var",Rolling.getHelp("r_var", "sample variance"))
                .put("r_varp",Rolling.getHelp("r_varp", "variance"))
                .put("r_std",Rolling.getHelp("r_std", "sample standard deviation"))
                .put("r_zscorep",Rolling.getHelp("r_zscorep", "z-score"))
                .put("r_zscore",Rolling.getHelp("r_zscore", "sample z-score"))
                .put("r_stdp",Rolling.getHelp("r_stdp", "standard deviation"))
                .put("correl",Rolling.getHelp("correl", "average correlation"))

            /* cross commands */
                .put("x_mean",Cross.getHelp("x_mean", "mean"))
                .put("x_max",Cross.getHelp("x_max", "maximum"))
                .put("x_min",Cross.getHelp("x_min", "minimum"))
                .put("x_sum",Cross.getHelp("x_sum", "sum"))
                .put("x_geomean",Cross.getHelp("x_geomean", "geometric mean"))
                .put("x_var",Cross.getHelp("x_var", "sample variance"))
                .put("x_varp",Cross.getHelp("x_varp", "variance"))
                .put("x_std",Cross.getHelp("x_std", "sample standard deviation"))
                .put("x_zscorep",Cross.getHelp("x_zscorep", "z-score"))
                .put("x_zscore",Cross.getHelp("x_zscore", "sample z-score"))
                .put("x_stdp",Cross.getHelp("x_stdp", "standard deviation"))
                
           /* other commands */
                .put("fill",Fill.getHelp())

           /* binary operators */
                .put("+",DoubleDoubleOperator.getHelp("+", "addition"))
                .put("-",DoubleDoubleOperator.getHelp("-", "subtraction"))
                .put("/",DoubleDoubleOperator.getHelp("/", "division"))
                .put("*",DoubleDoubleOperator.getHelp("*", "multiplication"))
                .put("%",DoubleDoubleOperator.getHelp("%", "modulo"))

                .build();
    public StandardVocabulary() {
    	
    }

	@Override
	public Map<String, CommandFactory> getCommandMap() {
		return commands;
	}

	@Override
	public Map<String, Supplier<CommandHelp>> getCommandHelpMap() {
		return commandHelp;
	}
    

}
