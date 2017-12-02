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

	private final LinkedList<LinkedList<Column<?,?>>> stacks = new LinkedList<>();
	private Optional<String> status = Optional.empty();
	private int baseDepth = 0;

	public Table() {
		stacks.add(new LinkedList<>());
	}
	
	public Table(String status) {
		this();
		this.status = Optional.of(status);
	}
	
	public LinkedList<LinkedList<Column<?,?>>> popStacks() {
		LinkedList<LinkedList<Column<?,?>>> l = new LinkedList<>();
		while(stacks.size() > baseDepth + 1 ) {
			l.addLast(stacks.removeLast());
		}
		if(l.size()==0) {
			l.add(stacks.isEmpty() ? new LinkedList<>() : stacks.removeLast());
		}
		return l;
	}
	
	public LinkedList<Column<?,?>> peekStack() {
		return stacks.getLast();
	}
	
	public void pushToBase(LinkedList<Column<?,?>> stack) {
		pushToBase(stack, false);
	}

	public void pushToBase(LinkedList<Column<?,?>> stack, boolean clearBase) {
		if(clearBase && stacks.size() > baseDepth) {
			stacks.get(baseDepth).clear();
		}
		while(stacks.size() <= baseDepth) {
			stacks.add(new LinkedList<>());
		}
		stacks.get(baseDepth).addAll(0, stack);
	}
	
	public void pushStacks(LinkedList<LinkedList<Column<?,?>>> stacks) {
		this.stacks.addAll(stacks);
	}
	
	public void incrementBase() {
		baseDepth++;
	}
	
	public void decrementBase() {
		baseDepth--;
		while(stacks.size() > baseDepth + 1) {
			stacks.get(stacks.size()-2).addAll(0,stacks.removeLast());
		}
	}
	
	/*
	 **Build-in**
	 * base after:				0		0
	 * stack.size() after:		1		1
	 * Step:			  		start	built-in
	 *
	 **Build-in w/ indexer before**
	 * base after:				0		0			0
	 * stack.size() after:	  	1		2			1
	 * Step:		  			start	[regular]	built-in
	 *
	 **Build-in w/ default**
	 * base after:				0		0			0
	 * stack.size() after:		1		2			1
	 * Step:			  		start	[default]	built-in
	 *
	 **Build-in w/ indexer before w/ default**
	 * base after:				0		0			0			0
	 * stack.size() after:	  	1		2			2			0
	 * Step:		  			start	[regular]	[default]	built-in
	 *
	 **Build-in w/ repeat**
	 * base after:				0		0			0
	 * stack.size() after:	  	1		5			0
	 * Step:		  			start	[regular+]	built-in
	 *
	 *
	 **Build-in w/ repeat w/ default**
	 * base after:				0		0			0			0
	 * stack.size() after:	  	1		5			5			0
	 * Step:		  			start	[regular+]	[default]	built-in
	 *
	 **Defined** 
	 * base after:				0		0			1			1		   1        0
	 * stack.size() after:	  	1		2			2			2		   1		0
	 * Step:		  			start	[regular]	[default]	[regular]  function definedend
	 * 
	 * default doesn't increase depth...unless starting depth 0
	 */
	
	public Optional<PeriodicRange<?>> getRange() {
		return peekStack().isEmpty() ? Optional.empty() : peekStack().getFirst().getRange(); 
	}
	
	public void setStatus(String s) {
		this.status = Optional.of(s);
	}
	
	public Optional<String> getStatus() {
		return status;
	}

	public List<String> getHeaders(boolean reverse) {
		return (reverse ? streamInReverse(peekStack()) : peekStack().stream())
				.map(Column::getHeader).collect(Collectors.toList());
	}

	public int getColumnCount() {
		return peekStack().size();
	}

	public int getRowCount() {
		return getRange().isPresent() ? (int) getRange().get().size() : 0;
	}

	public DoubleStream getSeries(int index, boolean reverse) {
		int i = reverse ? peekStack().size() - index - 1 : index;
		return getRange().isPresent() ? (DoubleStream) peekStack().get(i).rows() : DoubleStream.empty();
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
		sb.append(streamInReverse(peekStack()).map(Column::getHeader).collect(Collectors.joining(",","Date,","\n")));
		if(getRange().isPresent()) {
			List<LocalDate> d = getRange().get().dates();
			List<OfDouble> s = peekStack().stream().map(c -> (Column.OfDoubles) c).map(c -> c.rows())
					.map(DoubleStream::iterator).collect(Collectors.toList());
			for(int row = 0; row < getRange().get().size(); row++) {
				sb.append(d.get(row).format(dtf)).append(",");
				for (int col = peekStack().size() - 1; col > -1; col--) {
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
		double[][] series = new double[peekStack().size()][];
		for (int i = peekStack().size() - 1; i > -1; i--) {
			series[peekStack().size() - i - 1] = ((Column.OfDoubles)peekStack().get(i)).rows().toArray();
		}
		return Optional.of(series);
	}

	public Optional<double[][]> toRowMajorArray() {
		if (!getRange().isPresent()) {
			return Optional.empty();
		}
		double[][] table = new double[getRowCount()][getColumnCount()];
		for (int col = peekStack().size() - 1; col > -1; col--) {
			final int thisCol = col;
			AtomicInteger row = new AtomicInteger(0);
			((Column.OfDoubles)peekStack().get(col)).rows()
					.forEach(d -> table[row.getAndIncrement()][peekStack().size() - thisCol - 1] = d);
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
			String[][] table = new String[(int) getRange().get().size()][peekStack().size() + 1];
			for (int row = 0; row < getRange().get().size(); row++) {
				table[row][0] = dates.get(row).toString();
			}
			for (int col = 0; col < peekStack().size(); col++) {
				final int thisCol = col;
				AtomicInteger row = new AtomicInteger(0);
				peekStack().get(col).rowsAsText()
						.forEach(s -> table[row.getAndIncrement()][peekStack().size() - thisCol] = s);
			}
			return table;
		} else {
			String[][] table = new String[1][peekStack().size() + 1];
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
	
	
}
