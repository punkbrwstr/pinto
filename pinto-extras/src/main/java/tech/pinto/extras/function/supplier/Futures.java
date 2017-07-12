package tech.pinto.extras.function.supplier;


import java.text.MessageFormat;




import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.DoubleStream.Builder;

import tech.pinto.Indexer;
import tech.pinto.Pinto;
import tech.pinto.PintoSyntaxException;
import tech.pinto.function.CachedFunction;
import tech.pinto.function.ComposableFunction;
import tech.pinto.function.FunctionHelp;
import tech.pinto.function.ParameterType;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class Futures extends CachedFunction {
	
	
    final static String[] monthCodes = new String[]{"H","M","U","Z"};

    final private Pinto pinto;
    
	public static FunctionHelp getHelp(String name) {
		return new FunctionHelp.Builder(name)
				.outputs("1")
				.description("Calculates returns for front contract of given futures contract series.")
				.parameter("two-letter contract code")
				.parameter("bloomberg yellow key", "Comdty", null)
				.parameter("price modifier pinto function (acgbConvert)")
				.parameter("front contract criteria bloomberg field", "OPEN_INT", null)
				.parameter("price bloomberg field", "PX_LAST", null)
				.parameter("previous bloomberg field", "PX_LAST", null)
				.parameter("previous offset", "-1", null)
				.parameter("calc return", "true", null)
				.build();
	}

	public Futures(String name, Pinto pinto, ComposableFunction previousFunction, Indexer indexer) {
		super(name, previousFunction, indexer, ParameterType.arguments_required);
		this.pinto = pinto;
	}

	@Override
	protected List<String> getUncachedText() {
		return Arrays.asList(toString());
	}

	@Override
	protected <P extends Period> List<DoubleStream> getUncachedSeries(PeriodicRange<P> range) {
		String contractCode, contractYellowKey, criteriaFieldCode,
    							priceModifierFormula, priceFieldCode, pricePreviousCode;
		boolean calcReturn;
		int previousOffset;
        if(getArgs().length == 0) {
            throw new IllegalArgumentException("Wrong arguments for futures return");
        }
        contractCode = getArgs()[0].toUpperCase(); // don't trim because of "G "
        contractYellowKey = getArgs().length > 1 && !getArgs()[1].equals("") ? getArgs()[1].trim() : "Comdty";
        priceModifierFormula = getArgs().length > 2 ? getArgs()[2] : "";
        criteriaFieldCode = getArgs().length > 3 && ! getArgs()[3].equals("") ?
                getArgs()[3].trim().toUpperCase().replaceAll("\\s+","_") : "OPEN_INT";
        priceFieldCode = getArgs().length > 4 && !getArgs()[4].equals("") ? getArgs()[4].trim().toUpperCase().replaceAll("\\s+","_") : "PX_LAST";
        pricePreviousCode = getArgs().length > 5 && !getArgs()[5].equals("") ? getArgs()[5].trim().toUpperCase().replaceAll("\\s+","_") : priceFieldCode;
        previousOffset = getArgs().length > 6 && !getArgs()[6].equals("") ? Integer.parseInt(getArgs()[6]) : -1;
        calcReturn = getArgs().length > 7 ? Boolean.parseBoolean(getArgs()[7]) : true;
        int numberOfContracts = 0;
        // key is code ("Z-2010") and value is column index for data
        HashMap<String,Integer> contracts = new HashMap<>();
        // key is code and value is [start date, end date]
        HashMap<String,PeriodicRange<P>> contractStartEnd = new HashMap<>();
        // key is date and value is  [current code, next code]
        List<String[]> contractsForDate = new ArrayList<>(); 
        // figure out current and next contract for each date
        for(P p : range.values()) {
            LocalDate d = p.endDate();
            int monthIndex = (d.get(ChronoField.MONTH_OF_YEAR) - 1) / 3; 
            int year = d.getYear();
            String[] codes = new String[]{monthCodes[monthIndex] + "-" + year,
                monthCodes[monthIndex == 3 ? 0 : monthIndex + 1] + "-" + (year + (monthIndex == 3 ? 1 : 0))}; // array of current code and next
            contractsForDate.add(codes);
            for(String code : codes) {
                if(!contracts.containsKey(code)) {
                    contracts.put(code,numberOfContracts++);
                    contractStartEnd.put(code,range.periodicity().range(p, p, range.clearCache()));
                } else {
                    PeriodicRange<P> previousFirstLast = contractStartEnd.get(code);
                    contractStartEnd.put(code,range.periodicity().range(
                            previousFirstLast.start().isBefore(p) ? previousFirstLast.start() : p,
                            previousFirstLast.end().isAfter(p) ? previousFirstLast.end() : p,
                            		range.clearCache()));
                }
            }
        }
        // download values for criteria field and prices (starting one day before for prices)
        PeriodicRange<P> expandedRange = range.expand(-1);
        //final String formulaTemplate = "join(0,{3},bbg({0} Comdty,{1},fillprevious),{2},0) {4}";
        final String formulaTemplate = "0 bbg({0} {5},{1}) [0] flb(W-FRI) 0 join({3},{2}) {4}";
        double[][] criteria = new double[contracts.size()][];
        double[][] prices = new double[contracts.size()][];
        double[][] pricesPrev = new double[contracts.size()][];
        for(Map.Entry<String,PeriodicRange<P>> e : contractStartEnd.entrySet()) {
                    int contractRow = contracts.get(e.getKey());
            String[] monthYearCodes = e.getKey().split("-");
            String bbgContractCode = null;
            try {
                if(LocalDate.now().getYear() <= Integer.parseInt(monthYearCodes[1])) {
                    bbgContractCode = contractCode + monthYearCodes[0] + monthYearCodes[1].substring(3,4);
                } else {
                    bbgContractCode = contractCode + monthYearCodes[0] + monthYearCodes[1].substring(2,4);
                }
                criteria[contractRow] = runExpression( expandedRange,
                    MessageFormat.format(formulaTemplate,bbgContractCode,criteriaFieldCode,
                            e.getValue().end().next().endDate().toString(),
                            e.getValue().start().endDate().toString(),"", contractYellowKey));
                prices[contractRow] = runExpression( expandedRange,
                    MessageFormat.format(formulaTemplate,bbgContractCode,priceFieldCode,
                            e.getValue().end().next().endDate().toString(),
                            e.getValue().start().previous().endDate().toString(),
                            priceModifierFormula, contractYellowKey));
                if(pricePreviousCode.equals(priceFieldCode)) {
                    pricesPrev[contractRow] = prices[contractRow];
                } else {
                    pricesPrev[contractRow] = runExpression(expandedRange,
                        MessageFormat.format(formulaTemplate,bbgContractCode,pricePreviousCode,
                            e.getValue().end().next().endDate().toString(),
                            e.getValue().start().previous().endDate().toString(),
                            priceModifierFormula, contractYellowKey));
                }
            } catch(IllegalArgumentException badFormula) { // recent contract needs only one digit year
                throw badFormula;
            } catch(Exception wrongCode) { // recent contract needs only one digit year
                wrongCode.printStackTrace();
                bbgContractCode = contractCode + monthYearCodes[0] + monthYearCodes[1].substring(3,4);
                try {
                    criteria[contractRow] = runExpression(expandedRange,
                            MessageFormat.format(formulaTemplate,bbgContractCode,criteriaFieldCode,
                            e.getValue().end().next().endDate().toString(),
                            e.getValue().start().endDate().toString(),""));
                } catch(Exception notfound) {}
                try {
                    prices[contractRow] = runExpression(expandedRange,
                            MessageFormat.format(formulaTemplate,bbgContractCode,priceFieldCode,
                            e.getValue().end().next().endDate().toString(),
                            e.getValue().start().previous().endDate().toString(),
                            priceModifierFormula, contractYellowKey));
                } catch(Exception notfound) {}
            }
        }
        Builder d = DoubleStream.builder();
        for(int i = 1; i < expandedRange.size(); i++) {
            int row1 = contracts.get(contractsForDate.get(i-1)[0]); 
            int row2 = contracts.get(contractsForDate.get(i-1)[1]); 
            int currentRow = row1;
            if(isNG(prices[row1][i]) || isNG(prices[row1][i + previousOffset]) ) {
                currentRow = row2;
            } else if(isNG(prices[row2][i]) || isNG(prices[row2][i + previousOffset]) ) {
                currentRow = row1;
            } else if(isNG(pricesPrev[row2][i]) || isNG(pricesPrev[row2][i + previousOffset]) ) {
                currentRow = row1;
            } else if(isNG(pricesPrev[row1][i]) || isNG(pricesPrev[row1][i + previousOffset]) ) {
                currentRow = row2;
            } else if(isNG(criteria[row1][i]) && isNG(criteria[row2][i]) ) {
                currentRow = row1; // default to c1 if criteria are bad for both
            } else if(Double.isNaN(criteria[row1][i])) {
                currentRow = row2;
            } else if(Double.isNaN(criteria[row2][i])) {
                currentRow = row1;
            } else {
                currentRow = criteria[row1][i] >= criteria[row2][i] ? row1 : row2;
            }
            if(calcReturn) {
            	d.accept(prices[currentRow][i] / pricesPrev[currentRow][i + previousOffset] - 1.0d);
            } else {
            	d.accept(prices[currentRow][i]);
            }
        }
        return Arrays.asList(d.build());
	}

	@Override
	protected int columns() {
		return 1;
	}
	
	
	
    private boolean isNG(double d) {
        return Double.isNaN(d) || d == 0.0d || Double.isInfinite(d);
    }
    
    private <P extends Period> double[] runExpression(PeriodicRange<P> range, String exp) throws Exception {
    	try {
    		String e = exp + " eval(" + range.start().endDate().toString() + ","
    					+ range.end().endDate().toString() + "," + range.periodicity().code() + ")";
    		return pinto.execute(e).get(0).getColumnValues().get(0)
    				.getSeries().get().toArray();
    	} catch(PintoSyntaxException pse) {
    		throw new RuntimeException();
    	}
    }
}
