package tech.pinto;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import tech.pinto.function.FunctionHelp;
import tech.pinto.function.LambdaFunction;
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
import tech.pinto.function.intermediate.Reverse;
import tech.pinto.function.intermediate.Roll;
import tech.pinto.function.intermediate.Rolling;
import tech.pinto.function.intermediate.RollingCorrelation;
import tech.pinto.function.intermediate.UnaryOperator;
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
                .put("eval", new Name((n,c,i,s,a) -> new Evaluate(n,i,a), Evaluate::getHelp))
                .put("export", new Name((n,c,i,s,a) -> new Export(n,i,a),Export::getHelp))
                .put("def", new Name((n,c,i,s,a) -> new Define(n,c,i,s,a),Define::getHelp))
                .put("del", new Name((n,c,i,s,a) -> new Delete(n,c,i,a),Delete::getHelp))
                .put("help", new Name((n,c,i,s,a) -> new Help(n,c,i,a),Help::getHelp))
                .put("exec", new Name((n,c,i,s,a) -> new Execute(n,c,i,a),Execute::getHelp))
                .put("reset", new Name((n,c,i,s,a) -> new Reset(n,i,a),Reset::getHelp))
            /* stack manipulation functions */
                .put("label", new Name((n,c,i,s,a) -> new Label(n,i,a),Label::getHelp))
                .put("rev", new Name((n,c,i,s,a) -> new Reverse(n,i),Reverse::getHelp))
                .put("copy", new Name((n,c,i,s,a) -> new Copy(i,a),Copy::getHelp))
                .put("roll", new Name((n,c,i,s,a) -> new Roll(n,i,a),Roll::getHelp))
                .put("clear", new Name((n,c,i,s,a) -> new Clear(n,i,a),Clear::getHelp))
                .put("only", new Name((n,c,i,s,a) -> new Only(n,i,a),Only::getHelp))
            /* zeroth-order functions */
                .put("yhoo", new Name((n,c,i,s,a) -> new Yahoo(i,a),Yahoo::getHelp))
                .put("moon", new Name((n,c,i,s,a) -> new LambdaFunction(f -> n,
                		inputs -> range -> range.dates().stream().mapToDouble(d -> new tech.pinto.tools.MoonPhase(d).getPhase())),
                			n -> new FunctionHelp.Builder(n).description("Phase of the moon.").outputs("n + 1").build()))
            /* rolling window commands */
                .put("r_lag", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.first,false, a),n -> Rolling.getHelp(n,  "lag")))
	            .put("r_chg", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.change,false, a),n -> Rolling.getHelp(n,  "change")))
	            .put("r_chgpct", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.changepct,false, a),n -> Rolling.getHelp(n,  "change in percent")))
	            .put("r_chglog", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.changelog,false, a),n -> Rolling.getHelp(n,  "log change")))
	            .put("r_mean", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.average, true, a),n -> Rolling.getHelp(n,  "mean")))
	            .put("r_max", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.max, true, a),n -> Rolling.getHelp(n,  "maximum")))
	            .put("r_min", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.min, true, a),n -> Rolling.getHelp(n,  "minimum")))
	            .put("r_sum", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.sum, true, a),n -> Rolling.getHelp(n,  "sum")))
	            .put("r_geomean", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.geomean, true, a),n -> Rolling.getHelp(n,  "geometric mean")))
	            .put("r_var", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.var, true,a),n -> Rolling.getHelp(n,  "sample variance")))
	            .put("r_varp", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.varp, true, a),n -> Rolling.getHelp(n,  "variance")))
	            .put("r_std", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.stdev, true, a),n -> Rolling.getHelp(n,  "sample standard deviation")))
	            .put("r_stdp", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.stdevp, true, a),n -> Rolling.getHelp(n,  "standard deviation")))
	            .put("r_zscore", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.zscore, true, a),n -> Rolling.getHelp(n,  "sample z-score")))
	            .put("r_zscorep", new Name((n,c,i,s,a) -> new Rolling(n,i,DoubleCollectors.zscorep, true, a),n -> Rolling.getHelp(n,  "z-score")))
	            .put("r_correl", new Name((n,c,i,s,a) -> new RollingCorrelation(i,a),n -> Rolling.getHelp(n,  "average correlation")))
            /* expanding commands */
	            .put("e_chg", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.change, a),name -> Expanding.getHelp(name, "change")))
	            .put("e_chgpct", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.changepct, a),name -> Expanding.getHelp(name, "change in percent")))
	            .put("e_chglog", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.changelog, a),name -> Expanding.getHelp(name, "log change")))
	            .put("e_mean", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.average, a),name -> Expanding.getHelp(name, "mean")))
	            .put("e_max", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.max, a),name -> Expanding.getHelp(name, "maximum")))
	            .put("e_min", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.min, a),name -> Expanding.getHelp(name, "minimum")))
	            .put("e_sum", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.sum, a),name -> Expanding.getHelp(name, "sum")))
	            .put("e_geomean", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.geomean, a),name -> Expanding.getHelp(name, "geometric mean")))
	            .put("e_var", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.var, a),name -> Expanding.getHelp(name, "sample variance")))
	            .put("e_varp", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.varp, a),name -> Expanding.getHelp(name, "variance")))
	            .put("e_std", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.stdev, a),name -> Expanding.getHelp(name, "sample standard deviation")))
	            .put("e_stdp", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.stdevp, a),name -> Expanding.getHelp(name, "standard deviation")))
	            .put("e_zscore", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.zscore, a),name -> Expanding.getHelp(name, "sample z-score")))
	            .put("e_zscorep", new Name((n,c,i,s,a) -> new Expanding(n,i,DoubleCollectors.zscorep, a),name -> Expanding.getHelp(name, "z-score")))
            /* cross commands */
	            .put("x_mean", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.average, a),name -> Cross.getHelp(name, "mean")))
	            .put("x_max", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.max, a),name -> Cross.getHelp(name, "maximum")))
	            .put("x_min", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.min, a),name -> Cross.getHelp(name, "minimum")))
	            .put("x_sum", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.sum, a),name -> Cross.getHelp(name, "sum")))
	            .put("x_geomean", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.geomean, a),name -> Cross.getHelp(name, "geometric mean")))
	            .put("x_var", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.var,a),name -> Cross.getHelp(name, "sample variance")))
	            .put("x_varp", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.varp, a),name -> Cross.getHelp(name, "variance")))
	            .put("x_std", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.stdev, a),name -> Cross.getHelp(name, "sample standard deviation")))
	            .put("x_stdp", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.stdevp, a),name -> Cross.getHelp(name, "standard deviation")))
	            .put("x_zscore", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.zscore, a),name -> Cross.getHelp(name, "sample z-score")))
	            .put("x_zscorep", new Name((n,c,i,s,a) -> new Cross(n,i,DoubleCollectors.zscorep, a),name -> Cross.getHelp(name, "z-score")))
           /* other commands */
	            .put("fill", new Name((n,c,i,s,a) -> new Fill(n,i,a),Fill::getHelp))
	            .put("join", new Name((n,c,i,s,a) -> new Join(n,i,a),Join::getHelp))
	            .put("#", new Name((n,c,i,s,a) -> new Comment(n,i),Comment::getHelp))
           /* binary operators */
                .put("+", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x + y, a),n -> BinaryOperator.getHelp(n, "addition")))
                .put("-", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x - y, a),n -> BinaryOperator.getHelp(n, "subtraction")))
                .put("*", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x * y, a),n -> BinaryOperator.getHelp(n, "multiplication")))
                .put("/", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x / y, a),n -> BinaryOperator.getHelp(n, "division")))
                .put("%", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x % y, a),n -> BinaryOperator.getHelp(n, "modulo")))
                .put("==", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x == y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "equals")))
                .put("!=", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x != y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "not equals")))
                .put(">", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x > y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "greater than")))
                .put("<", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x < y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "less than")))
                .put(">=", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x >= y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "greater than or equal to")))
                .put("<=", new Name((n,c,i,s,a) -> new BinaryOperator(n,i, (x,y) -> x <= y ? 1.0 : 0.0, a),n -> BinaryOperator.getHelp(n, "less than or equal to")))
           /* unary operators */
                .put("abs", new Name((n,c,i,s,a) -> new UnaryOperator(n,i, x -> Math.abs(x)),n -> UnaryOperator.getHelp(n, "absolute value")))
                .put("neg", new Name((n,c,i,s,a) -> new UnaryOperator(n,i, x -> x * -1d),n -> UnaryOperator.getHelp(n, "negation")))
                .put("inv", new Name((n,c,i,s,a) -> new UnaryOperator(n,i, x -> 1.0 / x),n -> UnaryOperator.getHelp(n, "inverse")))
                .put("log", new Name((n,c,i,s,a) -> new UnaryOperator(n,i, x -> Math.log(x)),n -> UnaryOperator.getHelp(n, "natural log")))
                .put("exp", new Name((n,c,i,s,a) -> new UnaryOperator(n,i, x -> Math.exp(x)),n -> UnaryOperator.getHelp(n, "e^x")))
                .put("acgbConvert", new Name((n,c,j,s,a) -> new UnaryOperator(n,j,
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
