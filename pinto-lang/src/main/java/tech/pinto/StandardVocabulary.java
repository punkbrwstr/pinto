package tech.pinto;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import tech.pinto.function.FunctionFactory;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.intermediate.Clear;
import tech.pinto.function.intermediate.Copy;
import tech.pinto.function.intermediate.Cross;
import tech.pinto.function.intermediate.DoubleCollectors;
import tech.pinto.function.intermediate.BinaryOperator;
import tech.pinto.function.intermediate.Expanding;
import tech.pinto.function.intermediate.Fill;
import tech.pinto.function.intermediate.Label;
import tech.pinto.function.intermediate.Reverse;
import tech.pinto.function.intermediate.Roll;
import tech.pinto.function.intermediate.Rolling;
import tech.pinto.function.intermediate.RollingCorrelation;
import tech.pinto.function.intermediate.UnaryOperator;
import tech.pinto.function.supplier.MoonPhase;
import tech.pinto.function.supplier.Yahoo;
import tech.pinto.function.terminal.Delete;
import tech.pinto.function.terminal.Evaluate;
import tech.pinto.function.terminal.Execute;
import tech.pinto.function.terminal.Export;
import tech.pinto.function.terminal.Help;
import tech.pinto.function.terminal.Save;

public class StandardVocabulary implements Vocabulary {
    
    private final Map<String,FunctionFactory> commands = 
            new ImmutableMap.Builder<String, FunctionFactory>()
                .put("eval", (c,i,s,a) -> new Evaluate(i,a))
                .put("export", (c,i,s,a) -> new Export(i,a))
                .put("def", (c,i,s,a) -> new Save(c,s,a))
                .put("del", (c,i,s,a) -> new Delete(c,i,a))
                .put("help", (c,i,s,a) -> new Help(c,this,i,a))
                .put("exec", (c,i,s,a) -> new Execute(c,this,i,a))
                .put("+", (c,i,s,a) -> new BinaryOperator("+",i, (x,y) -> x + y))
                .put("-", (c,i,s,a) -> new BinaryOperator("-",i, (x,y) -> x - y))
                .put("*", (c,i,s,a) -> new BinaryOperator("*",i, (x,y) -> x * y))
                .put("/", (c,i,s,a) -> new BinaryOperator("/",i, (x,y) -> x / y))
                .put("%", (c,i,s,a) -> new BinaryOperator("%",i, (x,y) -> x % y))
                .put("==", (c,i,s,a) -> new BinaryOperator("==",i, (x,y) -> x == y ? 1.0 : 0.0))
                .put("!=", (c,i,s,a) -> new BinaryOperator("!=",i, (x,y) -> x != y ? 1.0 : 0.0))
                .put(">", (c,i,s,a) -> new BinaryOperator(">",i, (x,y) -> x > y ? 1.0 : 0.0))
                .put("<", (c,i,s,a) -> new BinaryOperator("<",i, (x,y) -> x < y ? 1.0 : 0.0))
                .put(">=", (c,i,s,a) -> new BinaryOperator(">=",i, (x,y) -> x >= y ? 1.0 : 0.0))
                .put("<=", (c,i,s,a) -> new BinaryOperator("<=",i, (x,y) -> x <= y ? 1.0 : 0.0))
                .put("abs", (c,i,s,a) -> new UnaryOperator("abs",i, x -> Math.abs(x)))
                .put("neg", (c,i,s,a) -> new UnaryOperator("neg",i, x -> x * -1d))
                .put("inv", (c,i,s,a) -> new UnaryOperator("inv",i, x -> 1.0 / x))
                .put("log", (c,i,s,a) -> new UnaryOperator("log",i, x -> Math.log(x)))
                .put("exp", (c,i,s,a) -> new UnaryOperator("exp",i, x -> Math.exp(x)))
                .put("acgbConvert", (c,j,s,a) -> new UnaryOperator("acgbConvert",j,
                    quote -> {
                        double TERM = 10, RATE = 6, price = 0; 
                        for (int i = 0; i < TERM * 2; i++) {
                            price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
                        }
                        price += 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
                        return price; }))
                .put("moon", (c,i,s,a) -> new MoonPhase())
                .put("label", (c,i,s,a) -> new Label(i,a))
                .put("rev", (c,i,s,a) -> new Reverse(i))
                .put("copy", (c,i,s,a) -> new Copy(i,a))
                .put("roll", (c,i,s,a) -> new Roll(i,a))
                .put("clear", (c,i,s,a) -> new Clear(i,a))
                .put("yhoo", (c,i,s,a) -> new Yahoo(c,i,a))
	            .put("last", (c,i,s,a) -> new Rolling("last",i,DoubleCollectors.last, false, a))
                .put("r_lag", (c,i,s,a) -> new Rolling("r_lag",i,DoubleCollectors.first,false, a))
	            .put("r_chg", (c,i,s,a) -> new Rolling("r_chg",i,DoubleCollectors.change,false, a))
	            .put("e_chg", (c,i,s,a) -> new Expanding("e_chg",i,DoubleCollectors.change, a))
	            .put("r_chgpct", (c,i,s,a) -> new Rolling("r_chgpct",i,DoubleCollectors.changepct,false, a))
	            .put("e_chgpct", (c,i,s,a) -> new Expanding("e_chgpct",i,DoubleCollectors.changepct, a))
	            .put("r_chglog", (c,i,s,a) -> new Rolling("r_chglog",i,DoubleCollectors.changelog,false, a))
	            .put("e_chglog", (c,i,s,a) -> new Expanding("e_chglog",i,DoubleCollectors.changelog, a))
	            .put("r_mean", (c,i,s,a) -> new Rolling("r_mean",i,DoubleCollectors.average, true, a))
	            .put("e_mean", (c,i,s,a) -> new Expanding("e_mean",i,DoubleCollectors.average, a))
	            .put("x_mean", (c,i,s,a) -> new Cross("x_mean",i,DoubleCollectors.average, a))
	            .put("r_max", (c,i,s,a) -> new Rolling("r_max",i,DoubleCollectors.max, true, a))
	            .put("e_max", (c,i,s,a) -> new Expanding("e_max",i,DoubleCollectors.max, a))
	            .put("x_max", (c,i,s,a) -> new Cross("x_max",i,DoubleCollectors.max, a))
	            .put("r_min", (c,i,s,a) -> new Rolling("r_max",i,DoubleCollectors.min, true, a))
	            .put("e_min", (c,i,s,a) -> new Expanding("e_max",i,DoubleCollectors.min, a))
	            .put("x_min", (c,i,s,a) -> new Cross("x_max",i,DoubleCollectors.min, a))
	            .put("r_sum", (c,i,s,a) -> new Rolling("r_sum",i,DoubleCollectors.sum, true, a))
	            .put("e_sum", (c,i,s,a) -> new Expanding("e_sum",i,DoubleCollectors.sum, a))
	            .put("x_sum", (c,i,s,a) -> new Cross("x_sum",i,DoubleCollectors.sum, a))
	            .put("r_geomean", (c,i,s,a) -> new Rolling("r_geomean",i,DoubleCollectors.geomean, true, a))
	            .put("e_geomean", (c,i,s,a) -> new Expanding("e_geomean",i,DoubleCollectors.geomean, a))
	            .put("x_geomean", (c,i,s,a) -> new Cross("x_geomean",i,DoubleCollectors.geomean, a))
	            .put("r_var", (c,i,s,a) -> new Rolling("r_var",i,DoubleCollectors.var, true,a))
	            .put("e_var", (c,i,s,a) -> new Expanding("e_var",i,DoubleCollectors.var, a))
	            .put("x_var", (c,i,s,a) -> new Cross("x_var",i,DoubleCollectors.var,a))
	            .put("r_varp", (c,i,s,a) -> new Rolling("r_varp",i,DoubleCollectors.varp, true, a))
	            .put("e_varp", (c,i,s,a) -> new Expanding("e_varp",i,DoubleCollectors.varp, a))
	            .put("x_varp", (c,i,s,a) -> new Cross("x_varp",i,DoubleCollectors.varp, a))
	            .put("r_std", (c,i,s,a) -> new Rolling("r_std",i,DoubleCollectors.stdev, true, a))
	            .put("e_std", (c,i,s,a) -> new Expanding("e_std",i,DoubleCollectors.stdev, a))
	            .put("x_std", (c,i,s,a) -> new Cross("x_std",i,DoubleCollectors.stdev, a))
	            .put("r_stdp", (c,i,s,a) -> new Rolling("r_stdp",i,DoubleCollectors.stdevp, true, a))
	            .put("e_stdp", (c,i,s,a) -> new Expanding("e_stdp",i,DoubleCollectors.stdevp, a))
	            .put("x_stdp", (c,i,s,a) -> new Cross("x_stdp",i,DoubleCollectors.stdevp, a))
	            .put("r_zscore", (c,i,s,a) -> new Rolling("r_zscore",i,DoubleCollectors.zscore, true, a))
	            .put("e_zscore", (c,i,s,a) -> new Expanding("e_zscore",i,DoubleCollectors.zscore, a))
	            .put("x_zscore", (c,i,s,a) -> new Cross("x_zscore",i,DoubleCollectors.zscore, a))
	            .put("r_zscorep", (c,i,s,a) -> new Rolling("r_zscorep",i,DoubleCollectors.zscorep, true, a))
	            .put("e_zscorep", (c,i,s,a) -> new Expanding("e_zscorep",i,DoubleCollectors.zscorep, a))
	            .put("x_zscorep", (c,i,s,a) -> new Cross("x_zscorep",i,DoubleCollectors.zscorep, a))
	            .put("fill", (c,i,s,a) -> new Fill(i,a))
	            .put("correl", (c,i,s,a) -> new RollingCorrelation(i,a))
                .build();

    private final Map<String,Supplier<FunctionHelp>> commandHelp = 
            new ImmutableMap.Builder<String, Supplier<FunctionHelp>>()
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
                .put("clear", Clear.getHelp())

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
                .put("x_stdp",Cross.getHelp("r_mean", "mean"))

            /* expanding commands */
                .put("e_mean",Rolling.getHelp("e_mean", "mean"))
                .put("e_max",Expanding.getHelp("e_max", "maximum"))
                .put("e_min",Expanding.getHelp("e_min", "minimum"))
                .put("e_sum",Expanding.getHelp("e_sum", "sum"))
                .put("e_geomean",Expanding.getHelp("e_geomean", "geometric mean"))
                .put("e_var",Expanding.getHelp("e_var", "sample variance"))
                .put("e_varp",Expanding.getHelp("e_varp", "variance"))
                .put("e_std",Expanding.getHelp("e_std", "sample standard deviation"))
                .put("e_zscorep",Expanding.getHelp("e_zscorep", "z-score"))
                .put("e_zscore",Expanding.getHelp("e_zscore", "sample z-score"))
                .put("e_stdp",Expanding.getHelp("e_stdp", "standard deviation"))
                
           /* other commands */
                .put("fill",Fill.getHelp())

           /* binary operators */
                .put("+",BinaryOperator.getHelp("+", "addition"))
                .put("-",BinaryOperator.getHelp("-", "subtraction"))
                .put("/",BinaryOperator.getHelp("/", "division"))
                .put("*",BinaryOperator.getHelp("*", "multiplication"))
                .put("%",BinaryOperator.getHelp("%", "modulo"))
                .put("==",BinaryOperator.getHelp("==", "equals"))
                .put("!=",BinaryOperator.getHelp("!=", "not equals"))
                .put(">",BinaryOperator.getHelp(">", "greater than"))
                .put("<",BinaryOperator.getHelp("<", "less than"))
                .put(">=",BinaryOperator.getHelp(">=", "greater than or equal to"))
                .put("<=",BinaryOperator.getHelp("<=", "less than or equal to"))
                
                .put("abs",UnaryOperator.getHelp("abs", "absolute value"))
                .put("neg",UnaryOperator.getHelp("neg", "negation"))
                .put("inv",UnaryOperator.getHelp("inv", "inverse"))
                .put("log",UnaryOperator.getHelp("log", "natural log"))
                

                .build();
    public StandardVocabulary() {
    	
    }

	@Override
	public Map<String, FunctionFactory> getCommandMap() {
		return commands;
	}

	@Override
	public Map<String, Supplier<FunctionHelp>> getCommandHelpMap() {
		return commandHelp;
	}
    

}
