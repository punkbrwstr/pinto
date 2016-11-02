package tech.pinto;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.intermediate.Clear;
import tech.pinto.function.intermediate.Comment;
import tech.pinto.function.intermediate.Copy;
import tech.pinto.function.intermediate.Cross;
import tech.pinto.function.intermediate.DoubleCollectors;
import tech.pinto.function.intermediate.BinaryOperator;
import tech.pinto.function.intermediate.Expanding;
import tech.pinto.function.intermediate.Fill;
import tech.pinto.function.intermediate.Join;
import tech.pinto.function.intermediate.Label;
import tech.pinto.function.intermediate.Only;
import tech.pinto.function.intermediate.Resample;
import tech.pinto.function.intermediate.Reverse;
import tech.pinto.function.intermediate.Roll;
import tech.pinto.function.intermediate.Rolling;
import tech.pinto.function.intermediate.RollingCorrelation;
import tech.pinto.function.intermediate.UnaryOperator;
import tech.pinto.function.supplier.Moon;
import tech.pinto.function.supplier.Yahoo;
import tech.pinto.function.terminal.Delete;
import tech.pinto.function.terminal.Evaluate;
import tech.pinto.function.terminal.Execute;
import tech.pinto.function.terminal.Export;
import tech.pinto.function.terminal.Help;
import tech.pinto.function.terminal.Reset;
import tech.pinto.function.terminal.Define;

public class StandardVocabulary extends Vocabulary {
    
    protected final Map<String,Name> names;
    
    public StandardVocabulary() {
    	names = new HashMap<String,Name>(new ImmutableMap.Builder<String, Name>()
            /* terminal functions */
                .put("eval", new Name((n,p,s,f,i,a) -> new Evaluate(n,s,f,i,a), Evaluate::getHelp))
                .put("export", new Name((n,p,s,f,i,a) -> new Export(n,s,f,i,a),Export::getHelp))
                .put("def", new Name((n,p,s,f,i,a) -> new Define(n,s,f,i,a),Define::getHelp))
                .put("del", new Name((n,p,s,f,i,a) -> new Delete(n,s,f,i,a),Delete::getHelp))
                .put("help", new Name((n,p,s,f,i,a) -> new Help(n,s,f,i,a),Help::getHelp))
                .put("exec", new Name((n,p,s,f,i,a) -> new Execute(n,p,s,f,i,a),Execute::getHelp))
                .put("reset", new Name((n,p,s,f,i,a) -> new Reset(n,s,f,i,a),Reset::getHelp))
            /* stack manipulation functions */
                .put("label", new Name((n,p,s,f,i,a) -> new Label(n,f,i,a),Label::getHelp))
                .put("rev", new Name((n,p,s,f,i,a) -> new Reverse(n,f,i,a),Reverse::getHelp))
                .put("copy", new Name((n,p,s,f,i,a) -> new Copy(n,f,i,a),Copy::getHelp))
                .put("roll", new Name((n,p,s,f,i,a) -> new Roll(n,f,i,a),Roll::getHelp))
                .put("clear", new Name((n,p,s,f,i,a) -> new Clear(n,f,i,a),Clear::getHelp))
                .put("only", new Name((n,p,s,f,i,a) -> new Only(n,f,i,a),Only::getHelp))
            /* zeroth-order functions */
                .put("yhoo", new Name((n,p,s,f,i,a) -> new Yahoo(n,f,i,a),Yahoo::getHelp))
                .put("moon", new Name((n,p,s,f,i,a) -> new Moon(n,f,i),
                			n -> new FunctionHelp.Builder(n).description("Phase of the moon.").outputs("n + 1").build()))
            /* rolling window commands */
                .put("r_lag", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.first,false, a),n -> Rolling.getHelp(n,  "lag")))
	            .put("r_chg", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.change,false, a),n -> Rolling.getHelp(n,  "change")))
	            .put("r_chgpct", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.changepct,false, a),n -> Rolling.getHelp(n,  "change in percent")))
	            .put("r_chglog", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.changelog,false, a),n -> Rolling.getHelp(n,  "log change")))
	            .put("r_mean", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.average, true, a),n -> Rolling.getHelp(n,  "mean")))
	            .put("r_max", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.max, true, a),n -> Rolling.getHelp(n,  "maximum")))
	            .put("r_min", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.min, true, a),n -> Rolling.getHelp(n,  "minimum")))
	            .put("r_sum", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.sum, true, a),n -> Rolling.getHelp(n,  "sum")))
	            .put("r_geomean", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.geomean, true, a),n -> Rolling.getHelp(n,  "geometric mean")))
	            .put("r_var", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.var, true,a),n -> Rolling.getHelp(n,  "sample variance")))
	            .put("r_varp", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.varp, true, a),n -> Rolling.getHelp(n,  "variance")))
	            .put("r_std", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.stdev, true, a),n -> Rolling.getHelp(n,  "sample standard deviation")))
	            .put("r_stdp", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.stdevp, true, a),n -> Rolling.getHelp(n,  "standard deviation")))
	            .put("r_zscore", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.zscore, true, a),n -> Rolling.getHelp(n,  "sample z-score")))
	            .put("r_zscorep", new Name((n,p,s,f,i,a) -> new Rolling(n,f,i,DoubleCollectors.zscorep, true, a),n -> Rolling.getHelp(n,  "z-score")))
	            .put("r_correl", new Name((n,p,s,f,i,a) -> new RollingCorrelation(n,f,i,a),n -> Rolling.getHelp(n,  "average correlation")))
            /* expanding commands */
	            .put("e_chg", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.change, a),name -> Expanding.getHelp(name, "change")))
	            .put("e_chgpct", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.changepct, a),name -> Expanding.getHelp(name, "change in percent")))
	            .put("e_chglog", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.changelog, a),name -> Expanding.getHelp(name, "log change")))
	            .put("e_mean", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.average, a),name -> Expanding.getHelp(name, "mean")))
	            .put("e_max", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.max, a),name -> Expanding.getHelp(name, "maximum")))
	            .put("e_min", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.min, a),name -> Expanding.getHelp(name, "minimum")))
	            .put("e_sum", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.sum, a),name -> Expanding.getHelp(name, "sum")))
	            .put("e_geomean", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.geomean, a),name -> Expanding.getHelp(name, "geometric mean")))
	            .put("e_var", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.var, a),name -> Expanding.getHelp(name, "sample variance")))
	            .put("e_varp", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.varp, a),name -> Expanding.getHelp(name, "variance")))
	            .put("e_std", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.stdev, a),name -> Expanding.getHelp(name, "sample standard deviation")))
	            .put("e_stdp", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.stdevp, a),name -> Expanding.getHelp(name, "standard deviation")))
	            .put("e_zscore", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.zscore, a),name -> Expanding.getHelp(name, "sample z-score")))
	            .put("e_zscorep", new Name((n,p,s,f,i,a) -> new Expanding(n,f,i,DoubleCollectors.zscorep, a),name -> Expanding.getHelp(name, "z-score")))
            /* cross commands */
	            .put("x_mean", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.average, a),name -> Cross.getHelp(name, "mean")))
	            .put("x_max", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.max, a),name -> Cross.getHelp(name, "maximum")))
	            .put("x_min", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.min, a),name -> Cross.getHelp(name, "minimum")))
	            .put("x_sum", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.sum, a),name -> Cross.getHelp(name, "sum")))
	            .put("x_geomean", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.geomean, a),name -> Cross.getHelp(name, "geometric mean")))
	            .put("x_var", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.var,a),name -> Cross.getHelp(name, "sample variance")))
	            .put("x_varp", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.varp, a),name -> Cross.getHelp(name, "variance")))
	            .put("x_std", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.stdev, a),name -> Cross.getHelp(name, "sample standard deviation")))
	            .put("x_stdp", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.stdevp, a),name -> Cross.getHelp(name, "standard deviation")))
	            .put("x_zscore", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.zscore, a),name -> Cross.getHelp(name, "sample z-score")))
	            .put("x_zscorep", new Name((n,p,s,f,i,a) -> new Cross(n,f,i,DoubleCollectors.zscorep, a),name -> Cross.getHelp(name, "z-score")))
           /* other commands */
	            .put("fill", new Name((n,p,s,f,i,a) -> new Fill(n,f,i,a),Fill::getHelp))
	            .put("join", new Name((n,p,s,f,i,a) -> new Join(n,f,i,a),Join::getHelp))
	            .put("resample", new Name((n,p,s,f,i,a) -> new Resample(n,f,i,a),Resample::getHelp))
	            .put("#", new Name((n,p,s,f,i,a) -> new Comment(n,f,i,a),Comment::getHelp))
           /* binary operators */
                .put("+", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x + y, a),n -> BinaryOperator.getHelp(n, "addition")))
                .put("-", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x - y, a),n -> BinaryOperator.getHelp(n, "subtraction")))
                .put("*", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x * y, a),n -> BinaryOperator.getHelp(n, "multiplication")))
                .put("/", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x / y, a),n -> BinaryOperator.getHelp(n, "division")))
                .put("%", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x % y, a),n -> BinaryOperator.getHelp(n, "modulo")))
                .put("==", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x == y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "equals")))
                .put("!=", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x != y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "not equals")))
                .put(">", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x > y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "greater than")))
                .put("<", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x < y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "less than")))
                .put(">=", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x >= y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "greater than or equal to")))
                .put("<=", new Name((n,p,s,f,i,a) -> new BinaryOperator(n,f,i, (x,y) -> x <= y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "less than or equal to")))
           /* unary operators */
                .put("abs", new Name((n,p,s,f,i,a) -> new UnaryOperator(n,f,i, x -> Math.abs(x)),n -> UnaryOperator.getHelp(n, "absolute value")))
                .put("neg", new Name((n,p,s,f,i,a) -> new UnaryOperator(n,f,i, x -> x * -1d),n -> UnaryOperator.getHelp(n, "negation")))
                .put("inv", new Name((n,p,s,f,i,a) -> new UnaryOperator(n,f,i, x -> 1.0 / x),n -> UnaryOperator.getHelp(n, "inverse")))
                .put("log", new Name((n,p,s,f,i,a) -> new UnaryOperator(n,f,i, x -> Math.log(x)),n -> UnaryOperator.getHelp(n, "natural log")))
                .put("exp", new Name((n,p,s,f,i,a) -> new UnaryOperator(n,f,i, x -> Math.exp(x)),n -> UnaryOperator.getHelp(n, "e^x")))
                .put("acgbConvert", new Name((n,p,s,f,d,a) -> new UnaryOperator(n,f,d,
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
