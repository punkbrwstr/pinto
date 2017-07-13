package tech.pinto;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import tech.pinto.time.PeriodicRange;

public class Table {

	private final LinkedList<Column> columns;
	private final Optional<PeriodicRange<?>> range;

	public Table(LinkedList<Column> columns) {
		this(columns, Optional.empty());
	}

	public Table(LinkedList<Column> columns, Optional<PeriodicRange<?>> range) {
		this.columns = columns;
		this.range = range;
	}

	public LinkedList<Column> getColumns() {
		return columns;
	}
	
	public Optional<PeriodicRange<?>> getRange() {
		return range;
	}

	public List<String> getHeaders() {
		return streamInReverse(columns).map(Column::getHeader).map(o -> o.orElse("")).collect(Collectors.toList());
	}
	
	public int getColumnCount() {
		return columns.size();
	}
	
	public Optional<Integer> getRowCount() {
		return range.isPresent() ? Optional.of((int)range.get().size()) : Optional.empty();
	}
	
	public Optional<DoubleStream> getSeries(int i) {
		return !range.isPresent() ? Optional.empty() : columns.get(i).getSeries(range.get());
	}
	
	public Optional<double[][]> toColumnMajorArray() {
		if(!range.isPresent()) {
			return Optional.empty();
		}
		double[][] series = new double[columns.size()][];
		double[] nullSeries = null;
		for (int i = columns.size() - 1; i > -1; i--) {
			Optional<DoubleStream> s = columns.get(i).getSeries(range.get());
			if (s.isPresent()) {
				series[columns.size() - i - 1] = s.get().toArray();
			} else {
				if (nullSeries == null) {
					nullSeries = new double[(int) range.get().size()];
					Arrays.fill(nullSeries, Double.NaN);
					series[columns.size() - i - 1] = nullSeries;
				}
			}
		}
		return Optional.of(series);
	}

	public Optional<double[][]> toRowMajorArray() {
		if(!range.isPresent()) {
			return Optional.empty();
		}
		double[][] table = new double[getRowCount().get()][getColumnCount()];
		for (int col = columns.size() - 1; col > -1; col--) {
			Optional<DoubleStream> colData = columns.get(col).getSeries(range.get());
			final int thisCol = col;
			AtomicInteger row = new AtomicInteger(0);
			if (colData.isPresent()) {
				colData.get().forEach(d -> table[row.getAndIncrement()][columns.size() - thisCol - 1] = d);
			} else {
				DoubleStream.iterate(Double.NaN, r -> Double.NaN).limit(range.get().size())
					.forEach(d -> table[row.getAndIncrement()][columns.size() - thisCol] = d);
			}
		}
		return Optional.of(table);
	}
	
	public String[] headerToText() {
		List<String> l = getHeaders();
		l.add(0, range.isPresent() ? "Date" : " ");
		return l.toArray(new String[] {});
	}
	
	public String[][] seriesToText(NumberFormat nf) {
		if(range.isPresent()) {
			List<LocalDate> dates = range.get().dates();
			String[][] table = new String[(int) range.get().size()][columns.size() + 1];
			for(int row = 0; row < range.get().size(); row++) {
				table[row][0] = dates.get(row).toString();
			}
			for(int col = 0; col < columns.size(); col++) {
				Optional<DoubleStream> colData = columns.get(col).getSeries(range.get());
				if(colData.isPresent()) { 
					final int thisCol = col;
					AtomicInteger row = new AtomicInteger(0);
					colData.get().forEach(d -> table[row.getAndIncrement()][columns.size() - thisCol] = nf.format(d));
				} else {
					for(int row = 0; row < range.get().size(); row++) {
						table[row][col + 1] = "";
 					}
				}
				
			}
			return table;
		} else {
			String[][] table = new String[1][columns.size()+1];
			Arrays.fill(table[0], " ");
			return table;
		}	
	}

	private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		Iterator<T> descendingIterator = input.descendingIterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(descendingIterator, Spliterator.ORDERED),
				false);
	}
}
