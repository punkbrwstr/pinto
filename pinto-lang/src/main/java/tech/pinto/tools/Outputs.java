package tech.pinto.tools;

import java.text.NumberFormat;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;

import tech.pinto.ColumnValues;
import tech.pinto.time.PeriodicRange;

public class Outputs {

	@SuppressWarnings("rawtypes")
	public static Collector<ColumnValues,ArrayList<ColumnValues>,StringTable> columnValuesCollector(
			PeriodicRange range) {
		return columnValuesCollector(NumberFormat.getInstance(),Optional.of(range));
	}

	public static Collector<ColumnValues,ArrayList<ColumnValues>,StringTable> columnValuesCollector(
			Optional<PeriodicRange<?>> range) {
		return columnValuesCollector(NumberFormat.getInstance(),range);
	}
	
	public static Collector<ColumnValues,ArrayList<ColumnValues>,StringTable> columnValuesCollector(
			NumberFormat nf, Optional<PeriodicRange<?>> range) {
		Supplier<ArrayList<ColumnValues>> supplier = ArrayList::new;
		BiConsumer<ArrayList<ColumnValues>,ColumnValues> accumulator = ArrayList::add;
		BinaryOperator<ArrayList<ColumnValues>> combiner = (left, right) -> {
			left.addAll(right);
			return left;
		};
		Function<ArrayList<ColumnValues>,StringTable> finisher = columns -> {
			String[] header = new String[columns.size() + 1];
			for(int col = 0; col < columns.size(); col++) {
				header[col + 1] = columns.get(col).getHeader().orElse("");
			}
			if(range.isPresent()) {
				List<LocalDate> dates = range.get().dates();
				header[0] = "Date";
				String[][] table = new String[(int) range.get().size()][columns.size() + 1];
				for(int row = 0; row < range.get().size(); row++) {
					table[row][0] = dates.get(row).toString();
				}
				for(int col = 0; col < columns.size(); col++) {
					Optional<DoubleStream> colData = columns.get(col).getSeries();
					if(colData.isPresent()) { 
						final int thisCol = col;
						AtomicInteger row = new AtomicInteger(0);
						colData.get().forEach(d -> table[row.getAndIncrement()][thisCol + 1] = nf.format(d));
					} else {
						for(int row = 0; row < range.get().size(); row++) {
							table[row][col + 1] = "";
	 					}
					}
					
				}
				return new StringTable(header,table);
			} else {
				header[0] = "";
				String[][] table = new String[1][columns.size()+1];
				Arrays.fill(table[0], " ");
				return new StringTable(header,table);
			}
		};
		return Collector.of(supplier, accumulator, combiner, finisher);
	}
	
	public static class StringTable {
		
		private final String[] header;
		private final String[][] cells;

		public StringTable(String[] header, String[][] cells) {
			this.header = header;
			this.cells = cells;
		}
		public String[] getHeader() { return header; }
		public String[][] getCells() { return cells; }
		
		
	}
}

