package tech.pinto;

import static java.util.stream.Collectors.toList;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableMap;
import com.jakewharton.fliptables.FlipTable;

import tech.pinto.time.PeriodicRange;

public class Table {

	private final LinkedList<Column> columns;
	private final Optional<PeriodicRange<?>> range;

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
		return streamInReverse(columns).map(Column::getHeader).collect(Collectors.toList());
	}

	public int getColumnCount() {
		return columns.size();
	}

	public int getRowCount() {
		return range.isPresent() ? (int) range.get().size() : 0;
	}

	public DoubleStream getSeries(int i) {
		return range.isPresent() ? columns.get(i).getCells(range.get()) : DoubleStream.empty();
	}

	public String toCsv() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		return toCsv(DateTimeFormatter.ISO_LOCAL_DATE,nf);
	}

	public String toCsv(NumberFormat nf) {
		return toCsv(DateTimeFormatter.ISO_LOCAL_DATE,nf);
	}

	public String toCsv(DateTimeFormatter dtf, NumberFormat nf) {
		StringBuilder sb = new StringBuilder();
		sb.append(streamInReverse(columns).map(Column::getHeader).collect(Collectors.joining(",","Date,","\n")));
		if(range.isPresent()) {
			List<LocalDate> d = range.get().dates();
			List<OfDouble> s = streamInReverse(columns).map(c -> c.getCells(range.get()))
					.map(DoubleStream::iterator).collect(Collectors.toList());
			for(int row = 0; row < range.get().size(); row++) {
				sb.append(d.get(row).format(dtf)).append(",");
				for (int col = columns.size() - 1; col > -1; col--) {
					sb.append(nf.format(s.get(col).nextDouble()));
					sb.append(col > 0 ? "," : "\n");
				}
			}
		}
		return sb.toString();
	}


	
	public Map<String,Object> toMap(boolean omitDates, boolean numbersAsStrings) {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder.put("header", getHeaders());
		if (range.isPresent()) {
			builder.put("date_range", getRange().get().asStringMap());
			if (!omitDates) {
				builder.put("index", getRange().get().dates().stream().map(LocalDate::toString)
						.collect(toList()));
			}
			builder.put("series", !numbersAsStrings ? toColumnMajorArray().get() :
					Stream.of(toColumnMajorArray().get()).map(DoubleStream::of)
						.map(ds -> ds.mapToObj(Double::toString).collect(toList())).collect(toList()));
		}
		return builder.build();
	}

	public Optional<double[][]> toColumnMajorArray() {
		if (!range.isPresent()) {
			return Optional.empty();
		}
		double[][] series = new double[columns.size()][];
		for (int i = columns.size() - 1; i > -1; i--) {
			series[columns.size() - i - 1] = columns.get(i).getCells(range.get()).toArray();
		}
		return Optional.of(series);
	}

	public Optional<double[][]> toRowMajorArray() {
		if (!range.isPresent()) {
			return Optional.empty();
		}
		double[][] table = new double[getRowCount()][getColumnCount()];
		for (int col = columns.size() - 1; col > -1; col--) {
			final int thisCol = col;
			AtomicInteger row = new AtomicInteger(0);
			columns.get(col).getCells(range.get())
					.forEach(d -> table[row.getAndIncrement()][columns.size() - thisCol - 1] = d);
		}
		return Optional.of(table);
	}

	public String[] headerToText() {
		List<String> l = getHeaders();
		l.add(0, range.isPresent() ? "Date" : " ");
		return l.toArray(new String[] {});
	}

	public String[][] seriesToText(NumberFormat nf) {
		if (range.isPresent()) {
			List<LocalDate> dates = range.get().dates();
			String[][] table = new String[(int) range.get().size()][columns.size() + 1];
			for (int row = 0; row < range.get().size(); row++) {
				table[row][0] = dates.get(row).toString();
			}
			for (int col = 0; col < columns.size(); col++) {
				final int thisCol = col;
				AtomicInteger row = new AtomicInteger(0);
				columns.get(col).getCells(range.get())
						.forEach(d -> table[row.getAndIncrement()][columns.size() - thisCol] = nf.format(d));
			}
			return table;
		} else {
			String[][] table = new String[1][columns.size() + 1];
			Arrays.fill(table[0], " ");
			return table;
		}
	}
	
	@Override
	public String toString() {
		return getConsoleText(NumberFormat.getInstance());
		
	}
	
	public String getConsoleText(NumberFormat nf) {
		if(range.isPresent()) {
			return FlipTable.of(headerToText(), seriesToText(nf));
		} else {
			return getHeaders().stream().collect(Collectors.joining("\\t"));
		}
	}
	

	private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		Iterator<T> descendingIterator = input.descendingIterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(descendingIterator, Spliterator.ORDERED),
				false);
	}
}
