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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableMap;
import com.jakewharton.fliptables.FlipTable;

import tech.pinto.time.PeriodicRange;
import tech.pinto.Pinto.Stack;

public class Table {

	private final LinkedList<Level> levels = new LinkedList<>();
	private Optional<String> status = Optional.empty();
	private Optional<PeriodicRange<?>> range = Optional.empty();
	private Optional<Stack> flattenedStack = Optional.empty();

	public Table() {
		levels.add(new Level(true));
	}
	
	public Table(String status) {
		this();
		this.status = Optional.of(status);
	}
	
	public List<Stack> takeTop() {
		if(!levels.peekFirst().isFunctionLevel()) {
			return levels.removeFirst().getStacks();
		} else {
			return levels.peekFirst().clearStacks();
		}
	}
	
	public void insertAtTop(Stack s) {
		List<Stack> l = new ArrayList<>();
		l.add(s);
		insertAtTop(l);
	}

	public void insertAtTop(List<Stack> l) {
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
	
	public void push(boolean isFunction, List<Stack> l) {
		levels.addFirst(new Level(isFunction, l));
	}
	
	public void clearTop() {
		levels.peekFirst().clearStacks();
	}
	
	public void collapseFunction() {
		boolean foundFunction = false;
		while(levels.size() > 1 && !foundFunction) {
			foundFunction = levels.peekFirst().isFunctionLevel();
			List<Stack> functionReturn = levels.removeFirst().getStacks();
			insertAtTop(functionReturn);
		}
	}
	
	public Stack flatten() {
		while(levels.size() > 1) {
			List<Column<?>> collapsed = levels.removeFirst().getStacks().stream().flatMap(LinkedList::stream).collect(Collectors.toList());
			levels.peekFirst().getStacks().get(0).addAll(0,collapsed);
		}
		flattenedStack =  Optional.of(levels.peekFirst().getStacks().get(0));
		return flattenedStack.get();
	}

	public void setRange(PeriodicRange<?> range) {
		this.range = Optional.of(range);
		if(!flattenedStack.isPresent()) {
			flatten();
		}
	}

	public PeriodicRange<?> getRange() {
		return range.orElseThrow(() -> new PintoSyntaxException("Cannot access table range before evaluating."));
	}
	
	public Stack getStack() {
		return flattenedStack.orElseThrow(() -> new PintoSyntaxException("Cannot access stack before evaluating."));
	}
	
	public void setStatus(String s) {
		this.status = Optional.of(s);
	}
	
	public Optional<String> getStatus() {
		return status;
	}

	public List<String> getHeaders(boolean reverse, boolean trace) {
		return (reverse ? streamInReverse(getStack()) : getStack().stream())
				.map(trace ? Column::getTrace : Column::getHeader).collect(Collectors.toList());
	}

	public int getColumnCount() {
		return getStack().size();
	}

	public int getRowCount() {
		return (int) getRange().size();
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
		sb.append(streamInReverse(getStack()).map(Column::getHeader).collect(Collectors.joining(",","Date,","\n")));
		List<LocalDate> d = getRange().dates();
		List<String[]> s = getStack().stream().map(c -> c.rowsAsStrings(getRange(), nf)).collect(Collectors.toList());
		for(int row = 0; row < getRange().size(); row++) {
			sb.append(d.get(row).format(dtf)).append(",");
			for (int col = getStack().size() - 1; col > -1; col--) {
				sb.append(s.get(col)[row]);
				sb.append(col > 0 ? "," : "\n");
			}
		}
		return sb.toString();
	}


	
	public Map<String,Object> toMap(boolean omitDates, boolean numbersAsStrings) {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder.put("header", getHeaders(true, false));
		builder.put("date_range", getRange().asStringMap());
		if (!omitDates) {
			builder.put("index", getRange().dates().stream().map(LocalDate::toString)
					.collect(toList()));
		}
		builder.put("series", !numbersAsStrings ? toColumnMajorArray() :
				Stream.of(toColumnMajorArray()).map(DoubleStream::of)
					.map(ds -> ds.mapToObj(Double::toString).collect(toList())).collect(toList()));
		return builder.build();
	}

	public double[][] toColumnMajorArray() {
		double[][] series = new double[getStack().size()][];
		var stack = getStack();
		for (int i = stack.size() - 1; i > -1; i--) {
			if(stack.get(i).getType().equals(double[].class)) {
				series[i] = getStack().get(i).cast(double[].class).rows(getRange());
			} else {
				series[i] = new double[(int) getRange().size()];
				if(stack.get(i).getType().equals(Double.class)) {
					Arrays.fill(series[i], stack.get(i).cast(Double.class).rows(null));
				} else {
					Arrays.fill(series[i], Double.NaN);
				}
			}
		}
		return series;
	}

	public double[][] toRowMajorArray() {
		double[][] m = toColumnMajorArray();
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
	}

	public String[] headerToText(boolean trace) {
		List<String> l = getHeaders(true, trace);
		l.add(0, range.isPresent() ? "Date" : " ");
		return l.toArray(new String[] {});
	}

	public String[][] seriesToText(NumberFormat nf) {
		if (range.isPresent()) {
			List<LocalDate> dates = getRange().dates();
			String[][] table = new String[(int) getRange().size()][getStack().size() + 1];
			List<String[]> s = getStack().stream().map(c -> c.rowsAsStrings(getRange(), nf)).collect(Collectors.toList());
			for (int row = 0; row < getRange().size(); row++) {
				table[row][0] = dates.get(row).toString();
				for (int col = 0; col < getStack().size(); col++) {
					table[row][col+1] = s.get(getStack().size() - 1 - col)[row];
				}
			}
			return table;
		} else {
			String[][] table = new String[1][getStack().size() + 1];
			Arrays.fill(table[0], " ");
			return table;
		}
	}
	
	public String getConsoleText(NumberFormat nf, boolean trace) {
		return FlipTable.of(headerToText(trace), seriesToText(nf));
	}
	
	public String getConsoleText(boolean trace) {
		return FlipTable.of(headerToText(trace), seriesToText(NumberFormat.getInstance()));
	}

	private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		Iterator<T> descendingIterator = input.descendingIterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(descendingIterator, Spliterator.ORDERED),
				false);
	}
	
	private static class Level {
		private final List<Stack> stacks;
		private final boolean functionLevel;
		
		public Level(boolean baseLevel) {
			this(baseLevel, new ArrayList<>());
			stacks.add(new Stack());
		}

		public Level(boolean functionLevel, List<Stack> stacks) {
			this.functionLevel = functionLevel;
			this.stacks = stacks;
		}
		
		public List<Stack> getStacks() {
			return stacks;
		}
		
		public boolean isFunctionLevel() {
			return functionLevel;
		}
		
		public List<Stack> clearStacks() {
			ArrayList<Stack> l = new ArrayList<>(stacks);
			stacks.clear();
			for(int i = 0; i < l.size(); i++) {
				stacks.add(new Stack());
			}
			return l;
		}
		
		@Override
		public String toString() {
			return "functionLevel: " + Boolean.toString(functionLevel) + ", stacks: " + stacks.toString();
		}
			
	}
	
}
