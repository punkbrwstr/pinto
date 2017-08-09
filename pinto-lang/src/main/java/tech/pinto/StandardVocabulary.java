package tech.pinto;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.functions.BinaryOperator;
import tech.pinto.function.functions.Clear;
import tech.pinto.function.functions.Copy;
import tech.pinto.function.functions.Cross;
import tech.pinto.function.functions.Expanding;
import tech.pinto.function.functions.Fill;
import tech.pinto.function.functions.HeaderAppend;
import tech.pinto.function.functions.HeaderFormat;
import tech.pinto.function.functions.HeaderPrepend;
import tech.pinto.function.functions.ImportCSV;
import tech.pinto.function.functions.Join;
import tech.pinto.function.functions.Label;
import tech.pinto.function.functions.Moon;
import tech.pinto.function.functions.Only;
import tech.pinto.function.functions.Range;
import tech.pinto.function.functions.Resample;
import tech.pinto.function.functions.Reverse;
import tech.pinto.function.functions.Roll;
import tech.pinto.function.functions.Rolling;
import tech.pinto.function.functions.RollingCorrelation;
import tech.pinto.function.functions.UnaryOperator;
import tech.pinto.function.functions.terminal.Define;
import tech.pinto.function.functions.terminal.Delete;
import tech.pinto.function.functions.terminal.Evaluate;
import tech.pinto.function.functions.terminal.Execute;
import tech.pinto.function.functions.terminal.Export;
import tech.pinto.function.functions.terminal.Help;
import tech.pinto.tools.DoubleCollectors;

public class StandardVocabulary extends Vocabulary {
    
    protected final Map<String,Name> names;
    
    public StandardVocabulary() {
    	names = new HashMap<String,Name>(new ImmutableMap.Builder<String, Name>()
            /* terminal functions */
                .put("eval", new Name((n,p,s,f,i) -> new Evaluate(n,s,f,i), Evaluate.HELP_BUILDER))
                .put("export", new Name((n,p,s,f,i) -> new Export(n,s,f,i),Export.HELP_BUILDER))
                .put("def", new Name((n,p,s,f,i) -> new Define(n,s,f,i),Define.HELP_BUILDER))
                .put("del", new Name((n,p,s,f,i) -> new Delete(n,s,f,i),Delete.HELP_BUILDER))
                .put("help", new Name((n,p,s,f,i) -> new Help(n,s,f,i),Help.HELP_BUILDER))
                .put("exec", new Name((n,p,s,f,i) -> new Execute(n,p,s,f,i),Execute.HELP_BUILDER))
            /* stack manipulation functions */
                .put("label", new Name((n,p,s,f,i) -> new Label(n,f,i),Label.HELP_BUILDER))
                .put("rev", new Name((n,p,s,f,i) -> new Reverse(n,f,i),Reverse.HELP_BUILDER))
                .put("copy", new Name((n,p,s,f,i) -> new Copy(n,f,i),Copy.HELP_BUILDER))
                .put("roll", new Name((n,p,s,f,i) -> new Roll(n,f,i),Roll.HELP_BUILDER))
                .put("clear", new Name((n,p,s,f,i) -> new Clear(n,f,i),Clear.HELP_BUILDER))
                .put("only", new Name((n,p,s,f,i) -> new Only(n,f,i),Only.HELP_BUILDER))
            /* header manipulation functions */
                .put("format", new Name((n,p,s,f,i) -> new HeaderFormat(n,f,i),HeaderFormat.HELP_BUILDER))
                .put("append", new Name((n,p,s,f,i) -> new HeaderAppend(n,f,i),HeaderAppend.HELP_BUILDER))
                .put("prepend", new Name((n,p,s,f,i) -> new HeaderPrepend(n,f,i),HeaderPrepend.HELP_BUILDER))
            /* zeroth-order functions */
                .put("range", new Name((n,p,s,f,i) -> new Range(n,f,i), Range.HELP_BUILDER))
                .put("read", new Name((n,p,s,f,i) -> new ImportCSV(n,f,i), ImportCSV.HELP_BUILDER))
                .put("moon", new Name((n,p,s,f,i) -> new Moon(n,f,i),
                			 new FunctionHelp.Builder().description("Phase of the moon.").outputs("n + 1")))
            /* rolling window commands */
                .put("r_lag", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.first,false),
	            		Rolling.HELP_BUILDER.description("Lags data")))
	            .put("r_chg", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.change,false),
	            		Rolling.HELP_BUILDER.formatDescription("change from beginning to end")))
	            .put("r_chgpct", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.changepct,false),
	            		Rolling.HELP_BUILDER.formatDescription("percent change from beginning to end")))
	            .put("r_chglog", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.changelog,false),
	            		Rolling.HELP_BUILDER.formatDescription("log change from beginning to end")))
	            .put("r_mean", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.average, true),
	            		Rolling.HELP_BUILDER.formatDescription("average")))
	            .put("r_max", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.max, true),
	            		Rolling.HELP_BUILDER.formatDescription("maximum")))
	            .put("r_min", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.min, true),
	            		Rolling.HELP_BUILDER.formatDescription("minimum")))
	            .put("r_sum", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.sum, true),
	            		Rolling.HELP_BUILDER.formatDescription("sum")))
	            .put("r_geomean", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.geomean, true),
	            		Rolling.HELP_BUILDER.formatDescription("geometric mean")))
	            .put("r_var", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.var, true),
	            		Rolling.HELP_BUILDER.formatDescription("sample variance")))
	            .put("r_varp", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.varp, true),
	            		Rolling.HELP_BUILDER.formatDescription("population variance")))
	            .put("r_std", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.stdev, true),
	            		Rolling.HELP_BUILDER.formatDescription("sample standard deviation")))
	            .put("r_stdp", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.stdevp, true),
	            		Rolling.HELP_BUILDER.formatDescription("population standard deviation")))
	            .put("r_zscore", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.zscore, true),
	            		Rolling.HELP_BUILDER.formatDescription("sample z-score")))
	            .put("r_zscorep", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.zscorep, true),
	            		Rolling.HELP_BUILDER.formatDescription("population z-score")))
	            .put("r_correl", new Name((n,p,s,f,i) -> new RollingCorrelation(n,f,i),
	            		Rolling.HELP_BUILDER.formatDescription("correlation")))
            /* expanding commands */
	            .put("e_chg", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.change),
	            		Expanding.HELP_BUILDER.description("Calculates change over an expanding window")))
	            .put("e_chgpct", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.changepct),
	            		Expanding.HELP_BUILDER.description("Calculates change in percent over an expanding window")))
	            .put("e_chglog", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.changelog),
	            		Expanding.HELP_BUILDER.description("Calculates log change over an expanding window")))
	            .put("e_mean", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.average),
	            		Expanding.HELP_BUILDER.description("Calculates mean over an expanding window")))
	            .put("e_max", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.max),
	            		Expanding.HELP_BUILDER.description("Calculates maximum over an expanding window")))
	            .put("e_min", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.min),
	            		Expanding.HELP_BUILDER.description("Calculates minimum over an expanding window")))
	            .put("e_sum", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.sum),
	            		Expanding.HELP_BUILDER.description("Calculates sum over an expanding window")))
	            .put("e_geomean", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.geomean),
	            		Expanding.HELP_BUILDER.description("Calculates geometric mean over an expanding window")))
	            .put("e_var", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.var),
	            		Expanding.HELP_BUILDER.description("Calculates sample variance over an expanding window")))
	            .put("e_varp", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.varp),
	            		Expanding.HELP_BUILDER.description("Calculates variance over an expanding window")))
	            .put("e_std", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.stdev),
	            		Expanding.HELP_BUILDER.description("Calculates sample standard deviation over an expanding window")))
	            .put("e_stdp", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.stdevp),
	            		Expanding.HELP_BUILDER.description("Calculates standard deviation over an expanding window")))
	            .put("e_zscore", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.zscore),
	            		Expanding.HELP_BUILDER.description("Calculates sample z-score over an expanding window")))
	            .put("e_zscorep", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.zscorep),
	            		Expanding.HELP_BUILDER.description("Calculates z-score over an expanding window")))
            /* cross commands */
	            .put("x_mean", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.average),
	            		Cross.HELP_BUILDER.formatDescription("mean")))
	            .put("x_max", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.max),
	            		Cross.HELP_BUILDER.formatDescription("maximum")))
	            .put("x_min", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.min),
	            		Cross.HELP_BUILDER.formatDescription("minimum")))
	            .put("x_sum", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.sum),
	            		Cross.HELP_BUILDER.formatDescription("sum")))
	            .put("x_geomean", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.geomean),
	            		Cross.HELP_BUILDER.formatDescription("geometric mean")))
	            .put("x_var", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.var),
	            		Cross.HELP_BUILDER.formatDescription("variance")))
	            .put("x_varp", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.varp),
	            		Cross.HELP_BUILDER.formatDescription("portfolio variance")))
	            .put("x_std", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.stdev),
	            		Cross.HELP_BUILDER.formatDescription("sample standard deviation")))
	            .put("x_stdp", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.stdevp),
	            		Cross.HELP_BUILDER.formatDescription("population standard deviation")))
	            .put("x_zscore", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.zscore),
	            		Cross.HELP_BUILDER.formatDescription("sample z-score")))
	            .put("x_zscorep", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.zscorep),
	            		Cross.HELP_BUILDER.formatDescription("population z-score")))
           /* other commands */
	            .put("fill", new Name((n,p,s,f,i) -> new Fill(n,f,i,false),Fill.FILL_HELP_BUILDER))
	            .put("flb", new Name((n,p,s,f,i) -> new Fill(n,f,i,true),Fill.LOOKBACK_HELP_BUILDER))
	            .put("join", new Name((n,p,s,f,i) -> new Join(n,f,i),Join.HELP_BUILDER))
	            .put("resample", new Name((n,p,s,f,i) -> new Resample(n,f,i),Resample.HELP_BUILDER))
	            //.put("#", new Name((n,p,s,f,i) -> new Comment(n,f,i),Comment.HELP_BUILDER))
           /* binary operators */
                .put("+", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x + y), BinaryOperator.HELP_BUILDER.formatDescription("Adds","to")))
                .put("-", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x - y), BinaryOperator.HELP_BUILDER.formatDescription("Subtracts","from")))
                .put("*", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x * y), BinaryOperator.HELP_BUILDER.formatDescription("Multiplies","with")))
                .put("/", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x / y), BinaryOperator.HELP_BUILDER.formatDescription("Divided","into")))
                .put("%", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x % y), BinaryOperator.HELP_BUILDER.formatDescription("Modulo","with")))
                .put("^", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> Math.pow(x, y)), BinaryOperator.HELP_BUILDER.formatDescription("","to")))
                .put("==", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x == y ? 1.0 : 0.0), BinaryOperator.HELP_BUILDER.formatDescription("Applies","as an exponent to")))
                .put("!=", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x != y ? 1.0 : 0.0), BinaryOperator.HELP_BUILDER.formatDescription("Tests for inequality","with")))
                .put(">", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x > y ? 1.0 : 0.0), BinaryOperator.HELP_BUILDER.formatDescription("Tests for greater than","with")))
                .put("<", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x < y ? 1.0 : 0.0), BinaryOperator.HELP_BUILDER.formatDescription("Tests for less than","with")))
                .put(">=", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x >= y ? 1.0 : 0.0), BinaryOperator.HELP_BUILDER.formatDescription("Tests for greater than or equal to","with")))
                .put("<=", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x <= y ? 1.0 : 0.0), BinaryOperator.HELP_BUILDER.formatDescription("Tests for less than or equal to","with")))
           /* unary operators */
                .put("abs", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> Math.abs(x)),UnaryOperator.HELP_BUILDER.formatDescription( "absolute value")))
                .put("neg", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> x * -1d),UnaryOperator.HELP_BUILDER.formatDescription( "negation")))
                .put("inv", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> 1.0 / x),UnaryOperator.HELP_BUILDER.formatDescription( "inverse")))
                .put("log", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> Math.log(x)),UnaryOperator.HELP_BUILDER.formatDescription( "natural log")))
                .put("exp", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> Math.exp(x)),UnaryOperator.HELP_BUILDER.formatDescription( "e^x")))
                .put("acgbConvert", new Name((n,p,s,f,d) -> new UnaryOperator(n,f,d,
                    quote -> {
                        double TERM = 10, RATE = 6, price = 0; 
                        for (int i = 0; i < TERM * 2; i++) {
                            price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
                        }
                        price += 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
                        return price; }),UnaryOperator.HELP_BUILDER.formatDescription( "Australian bond futures quote to price conversion")))
                .build());

    }

	@Override
	protected Map<String, Name> getNameMap() {
		return names;
	}

}
