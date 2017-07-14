package tech.pinto;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.functions.BinaryOperator;
import tech.pinto.function.functions.Clear;
import tech.pinto.function.functions.Comment;
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
                .put("eval", new Name((n,p,s,f,i) -> new Evaluate(n,s,f,i), Evaluate::getHelp))
                .put("export", new Name((n,p,s,f,i) -> new Export(n,s,f,i),Export::getHelp))
                .put("def", new Name((n,p,s,f,i) -> new Define(n,s,f,i),Define::getHelp))
                .put("del", new Name((n,p,s,f,i) -> new Delete(n,s,f,i),Delete::getHelp))
                .put("help", new Name((n,p,s,f,i) -> new Help(n,s,f,i),Help::getHelp))
                .put("exec", new Name((n,p,s,f,i) -> new Execute(n,p,s,f,i),Execute::getHelp))
            /* stack manipulation functions */
                .put("label", new Name((n,p,s,f,i) -> new Label(n,f,i),Label::getHelp))
                .put("rev", new Name((n,p,s,f,i) -> new Reverse(n,f,i),Reverse::getHelp))
                .put("copy", new Name((n,p,s,f,i) -> new Copy(n,f,i),Copy::getHelp))
                .put("roll", new Name((n,p,s,f,i) -> new Roll(n,f,i),Roll::getHelp))
                .put("clear", new Name((n,p,s,f,i) -> new Clear(n,f,i),Clear::getHelp))
                .put("only", new Name((n,p,s,f,i) -> new Only(n,f,i),Only::getHelp))
            /* header manipulation functions */
                .put("format", new Name((n,p,s,f,i) -> new HeaderFormat(n,f,i),HeaderFormat::getHelp))
                .put("append", new Name((n,p,s,f,i) -> new HeaderAppend(n,f,i),HeaderAppend::getHelp))
                .put("prepend", new Name((n,p,s,f,i) -> new HeaderPrepend(n,f,i),HeaderPrepend::getHelp))
            /* zeroth-order functions */
                .put("range", new Name((n,p,s,f,i) -> new Range(n,f,i), Range::getHelp))
                .put("read", new Name((n,p,s,f,i) -> new ImportCSV(n,f,i), ImportCSV::getHelp))
                .put("moon", new Name((n,p,s,f,i) -> new Moon(n,f,i),
                			n -> new FunctionHelp.Builder(n).description("Phase of the moon.").outputs("n + 1").build()))
            /* rolling window commands */
                .put("r_lag", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.first,false),n -> Rolling.getHelp(n,  "lag")))
	            .put("r_chg", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.change,false),n -> Rolling.getHelp(n,  "change")))
	            .put("r_chgpct", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.changepct,false),n -> Rolling.getHelp(n,  "change in percent")))
	            .put("r_chglog", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.changelog,false),n -> Rolling.getHelp(n,  "log change")))
	            .put("r_mean", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.average, true),n -> Rolling.getHelp(n,  "mean")))
	            .put("r_max", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.max, true),n -> Rolling.getHelp(n,  "maximum")))
	            .put("r_min", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.min, true),n -> Rolling.getHelp(n,  "minimum")))
	            .put("r_sum", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.sum, true),n -> Rolling.getHelp(n,  "sum")))
	            .put("r_geomean", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.geomean, true),n -> Rolling.getHelp(n,  "geometric mean")))
	            .put("r_var", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.var, true),n -> Rolling.getHelp(n,  "sample variance")))
	            .put("r_varp", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.varp, true),n -> Rolling.getHelp(n,  "variance")))
	            .put("r_std", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.stdev, true),n -> Rolling.getHelp(n,  "sample standard deviation")))
	            .put("r_stdp", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.stdevp, true),n -> Rolling.getHelp(n,  "standard deviation")))
	            .put("r_zscore", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.zscore, true),n -> Rolling.getHelp(n,  "sample z-score")))
	            .put("r_zscorep", new Name((n,p,s,f,i) -> new Rolling(n,f,i,DoubleCollectors.zscorep, true),n -> Rolling.getHelp(n,  "z-score")))
	            .put("r_correl", new Name((n,p,s,f,i) -> new RollingCorrelation(n,f,i),n -> Rolling.getHelp(n,  "average correlation")))
            /* expanding commands */
	            .put("e_chg", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.change),name -> Expanding.getHelp(name, "change")))
	            .put("e_chgpct", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.changepct),name -> Expanding.getHelp(name, "change in percent")))
	            .put("e_chglog", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.changelog),name -> Expanding.getHelp(name, "log change")))
	            .put("e_mean", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.average),name -> Expanding.getHelp(name, "mean")))
	            .put("e_max", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.max),name -> Expanding.getHelp(name, "maximum")))
	            .put("e_min", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.min),name -> Expanding.getHelp(name, "minimum")))
	            .put("e_sum", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.sum),name -> Expanding.getHelp(name, "sum")))
	            .put("e_geomean", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.geomean),name -> Expanding.getHelp(name, "geometric mean")))
	            .put("e_var", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.var),name -> Expanding.getHelp(name, "sample variance")))
	            .put("e_varp", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.varp),name -> Expanding.getHelp(name, "variance")))
	            .put("e_std", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.stdev),name -> Expanding.getHelp(name, "sample standard deviation")))
	            .put("e_stdp", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.stdevp),name -> Expanding.getHelp(name, "standard deviation")))
	            .put("e_zscore", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.zscore),name -> Expanding.getHelp(name, "sample z-score")))
	            .put("e_zscorep", new Name((n,p,s,f,i) -> new Expanding(n,f,i,DoubleCollectors.zscorep),name -> Expanding.getHelp(name, "z-score")))
            /* cross commands */
	            .put("x_mean", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.average),name -> Cross.getHelp(name, "mean")))
	            .put("x_max", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.max),name -> Cross.getHelp(name, "maximum")))
	            .put("x_min", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.min),name -> Cross.getHelp(name, "minimum")))
	            .put("x_sum", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.sum),name -> Cross.getHelp(name, "sum")))
	            .put("x_geomean", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.geomean),name -> Cross.getHelp(name, "geometric mean")))
	            .put("x_var", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.var),name -> Cross.getHelp(name, "sample variance")))
	            .put("x_varp", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.varp),name -> Cross.getHelp(name, "variance")))
	            .put("x_std", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.stdev),name -> Cross.getHelp(name, "sample standard deviation")))
	            .put("x_stdp", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.stdevp),name -> Cross.getHelp(name, "standard deviation")))
	            .put("x_zscore", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.zscore),name -> Cross.getHelp(name, "sample z-score")))
	            .put("x_zscorep", new Name((n,p,s,f,i) -> new Cross(n,f,i,DoubleCollectors.zscorep),name -> Cross.getHelp(name, "z-score")))
           /* other commands */
	            .put("fill", new Name((n,p,s,f,i) -> new Fill(n,f,i,false),Fill::getHelp))
	            .put("flb", new Name((n,p,s,f,i) -> new Fill(n,f,i,true),Fill::getLookbackHelp))
	            .put("join", new Name((n,p,s,f,i) -> new Join(n,f,i),Join::getHelp))
	            .put("resample", new Name((n,p,s,f,i) -> new Resample(n,f,i),Resample::getHelp))
	            .put("#", new Name((n,p,s,f,i) -> new Comment(n,f,i),Comment::getHelp))
           /* binary operators */
                .put("+", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x + y),n -> BinaryOperator.getHelp(n, "addition")))
                .put("-", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x - y),n -> BinaryOperator.getHelp(n, "subtraction")))
                .put("*", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x * y),n -> BinaryOperator.getHelp(n, "multiplication")))
                .put("/", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x / y),n -> BinaryOperator.getHelp(n, "division")))
                .put("%", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x % y),n -> BinaryOperator.getHelp(n, "modulo")))
                .put("^", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> Math.pow(x, y)),n -> BinaryOperator.getHelp(n, "power of")))
                .put("==", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x == y ? 1.0 : 0.0),n -> BinaryOperator.getHelp(n, "equals")))
                .put("!=", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x != y ? 1.0 : 0.0),n -> BinaryOperator.getHelp(n, "not equals")))
                .put(">", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x > y ? 1.0 : 0.0),n -> BinaryOperator.getHelp(n, "greater than")))
                .put("<", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x < y ? 1.0 : 0.0),n -> BinaryOperator.getHelp(n, "less than")))
                .put(">=", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x >= y ? 1.0 : 0.0),n -> BinaryOperator.getHelp(n, "greater than or equal to")))
                .put("<=", new Name((n,p,s,f,i) -> new BinaryOperator(n,f,i, (x,y) -> x <= y ? 1.0 : 0.0),n -> BinaryOperator.getHelp(n, "less than or equal to")))
           /* unary operators */
                .put("abs", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> Math.abs(x)),n -> UnaryOperator.getHelp(n, "absolute value")))
                .put("neg", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> x * -1d),n -> UnaryOperator.getHelp(n, "negation")))
                .put("inv", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> 1.0 / x),n -> UnaryOperator.getHelp(n, "inverse")))
                .put("log", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> Math.log(x)),n -> UnaryOperator.getHelp(n, "natural log")))
                .put("exp", new Name((n,p,s,f,i) -> new UnaryOperator(n,f,i, x -> Math.exp(x)),n -> UnaryOperator.getHelp(n, "e^x")))
                .put("acgbConvert", new Name((n,p,s,f,d) -> new UnaryOperator(n,f,d,
                    quote -> {
                        double TERM = 10, RATE = 6, price = 0; 
                        for (int i = 0; i < TERM * 2; i++) {
                            price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
                        }
                        price += 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
                        return price; }),n -> UnaryOperator.getHelp(n, "Australian bond futures quote to price conversion")))
                .build());

    }

	@Override
	protected Map<String, Name> getNameMap() {
		return names;
	}

}
