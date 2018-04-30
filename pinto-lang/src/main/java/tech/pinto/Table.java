package tech.pinto;

import static java.util.stream.Collectors.toList;


import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

	private final LinkedList<Level> levels = new LinkedList<>();
	private Optional<String> status = Optional.empty();

	public Table() {
		levels.add(new Level(true));
	}
	
	public Table(String status) {
		this();
		this.status = Optional.of(status);
	}
	
	public List<LinkedList<Column<?,?>>> takeTop() {
		if(!levels.peekFirst().isFunctionLevel()) {
			return levels.removeFirst().getStacks();
		} else {
			return levels.peekFirst().clearStacks();
		}
	}
	
	public void insertAtTop(LinkedList<Column<?,?>> s) {
		List<LinkedList<Column<?,?>>> l = new ArrayList<>();
		l.add(s);
		insertAtTop(l);
	}

	public void insertAtTop(List<LinkedList<Column<?,?>>> l) {
		if(levels.peekFirst().getStacks().size() == 1) {
			levels.peekFirst().getStacks().get(0).addAll(0, l.stream().flatMap(LinkedList::stream).collect(Collectors.toList()));
		} else if(l.size() > 1) {
			if(levels.peekFirst().getStacks().size() == l.size()) {
				for(int i = 0; i < l.size(); i++) {
					levels.peekFirst().getStacks().get(i).addAll(0,l.get(i));
				}
			} else {
				throw new PintoSyntaxException("Mismatching repeated stack size");
			}
		}
	}
	
	public void push(boolean isFunction, List<LinkedList<Column<?,?>>> l) {
		levels.addFirst(new Level(isFunction, l));
	}
	
	public void clearTop() {
		levels.peekFirst().clearStacks();
	}
	
	public void collapse() {
		while(!levels.peekFirst().isFunctionLevel()) {
			insertAtTop(levels.removeFirst().getStacks());
		}
	}
	
	public void collapseFunction() {
		boolean foundFunction = false;
		while(!foundFunction) {
			foundFunction = levels.peekFirst().isFunctionLevel();
			List<Column<?,?>> collapsed = levels.removeFirst().getStacks().stream().flatMap(LinkedList::stream).collect(Collectors.toList());
			levels.peekFirst().getStacks().get(0).addAll(0,collapsed);
		}
	}
	
	public LinkedList<Column<?,?>> flatten() {
		while(levels.size() > 1) {
			List<Column<?,?>> collapsed = levels.removeFirst().getStacks().stream().flatMap(LinkedList::stream).collect(Collectors.toList());
			levels.peekFirst().getStacks().get(0).addAll(0,collapsed);
		}
		return levels.peekFirst().getStacks().get(0);
	}
	
	public Optional<PeriodicRange<?>> getRange() {
		return flatten().isEmpty() ? Optional.empty() : flatten().getFirst().getRange(); 
	}
	
	public void setStatus(String s) {
		this.status = Optional.of(s);
	}
	
	public Optional<String> getStatus() {
		return status;
	}

	public List<String> getHeaders(boolean reverse) {
		return (reverse ? streamInReverse(flatten()) : flatten().stream())
				.map(Column::getHeader).collect(Collectors.toList());
	}

	public int getColumnCount() {
		return flatten().size();
	}

	public int getRowCount() {
		return getRange().isPresent() ? (int) getRange().get().size() : 0;
	}

	public DoubleStream getSeries(int index, boolean reverse) {
		int i = reverse ? flatten().size() - index - 1 : index;
		return getRange().isPresent() ? (DoubleStream) flatten().get(i).rows() : DoubleStream.empty();
	}

	public String toCsv() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
                nf.setMaximumFractionDigits(10);
		return toCsv(DateTimeFormatter.ISO_LOCAL_DATE,nf);
	}

	public String toCsv(NumberFormat nf) {
		return toCsv(DateTimeFormatter.ISO_LOCAL_DATE,nf);
	}

	public String toCsv(DateTimeFormatter dtf, NumberFormat nf) {
		StringBuilder sb = new StringBuilder();
		sb.append(streamInReverse(flatten()).map(Column::getHeader).collect(Collectors.joining(",","Date,","\n")));
		if(getRange().isPresent()) {
			List<LocalDate> d = getRange().get().dates();
			List<OfDouble> s = flatten().stream().map(c -> (Column.OfDoubles) c).map(c -> c.rows())
					.map(DoubleStream::iterator).collect(Collectors.toList());
			for(int row = 0; row < getRange().get().size(); row++) {
				sb.append(d.get(row).format(dtf)).append(",");
				for (int col = flatten().size() - 1; col > -1; col--) {
					sb.append(nf.format(s.get(col).nextDouble()));
					sb.append(col > 0 ? "," : "\n");
				}
			}
		}
		return sb.toString();
	}


	
	public Map<String,Object> toMap(boolean omitDates, boolean numbersAsStrings) {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder.put("header", getHeaders(true));
		if (getRange().isPresent()) {
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
		if (!getRange().isPresent()) {
			return Optional.empty();
		}
		double[][] series = new double[flatten().size()][];
		for (int i = flatten().size() - 1; i > -1; i--) {
			series[flatten().size() - i - 1] = ((Column.OfDoubles)flatten().get(i)).rows().toArray();
		}
		return Optional.of(series);
	}

	public Optional<double[][]> toRowMajorArray() {
		if (!getRange().isPresent()) {
			return Optional.empty();
		}
		double[][] table = new double[getRowCount()][getColumnCount()];
		for (int col = flatten().size() - 1; col > -1; col--) {
			final int thisCol = col;
			AtomicInteger row = new AtomicInteger(0);
			((Column.OfDoubles)flatten().get(col)).rows()
					.forEach(d -> table[row.getAndIncrement()][flatten().size() - thisCol - 1] = d);
		}
		return Optional.of(table);
	}

	public String[] headerToText() {
		List<String> l = getHeaders(true);
		l.add(0, getRange().isPresent() ? "Date" : " ");
		return l.toArray(new String[] {});
	}

	public String[][] seriesToText(NumberFormat nf) {
		if (getRange().isPresent()) {
			List<LocalDate> dates = getRange().get().dates();
			String[][] table = new String[(int) getRange().get().size()][flatten().size() + 1];
			for (int row = 0; row < getRange().get().size(); row++) {
				table[row][0] = dates.get(row).toString();
			}
			for (int col = 0; col < flatten().size(); col++) {
				final int thisCol = col;
				AtomicInteger row = new AtomicInteger(0);
				flatten().get(col).rowsAsText()
						.forEach(s -> table[row.getAndIncrement()][flatten().size() - thisCol] = s);
			}
			return table;
		} else {
			String[][] table = new String[1][flatten().size() + 1];
			Arrays.fill(table[0], " ");
			return table;
		}
	}
	
	@Override
	public String toString() {
		return getConsoleText(NumberFormat.getInstance());
		
	}
	
	public String getConsoleText(NumberFormat nf) {
		if(getRange().isPresent()) {
			return FlipTable.of(headerToText(), seriesToText(nf));
		} else {
			return getHeaders(true).stream().collect(Collectors.joining("\\t"));
		}
	}
	

	private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		Iterator<T> descendingIterator = input.descendingIterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(descendingIterator, Spliterator.ORDERED),
				false);
	}
	
	private static class Level {
		private final List<LinkedList<Column<?,?>>> stacks;
		private final boolean functionLevel;
		
		public Level(boolean baseLevel) {
			this(baseLevel, new ArrayList<>());
			stacks.add(new LinkedList<>());
		}

		public Level(boolean functionLevel, List<LinkedList<Column<?,?>>> stacks) {
			this.functionLevel = functionLevel;
			this.stacks = stacks;
		}
		
		public List<LinkedList<Column<?,?>>> getStacks() {
			return stacks;
		}
		
		public boolean isFunctionLevel() {
			return functionLevel;
		}
		
		public List<LinkedList<Column<?,?>>> clearStacks() {
			ArrayList<LinkedList<Column<?,?>>> l = new ArrayList<>(stacks);
			stacks.clear();
			for(int i = 0; i < l.size(); i++) {
				stacks.add(new LinkedList<>());
			}
			return l;
		}
			
	}
	
}
