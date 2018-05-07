package tech.pinto;

import static tech.pinto.Pinto.toTableConsumer;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.PrimitiveIterator.OfDouble;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;
import tech.pinto.tools.DoubleCollector;
import tech.pinto.tools.DoubleCollectors;

public class StandardVocabulary extends Vocabulary {
    
    protected final Map<String,Name> names;
	private Optional<MessageFormat> chartHTML = Optional.empty();
    
	@SuppressWarnings("unchecked")
	public StandardVocabulary() {
    	names = new HashMap<String,Name>();
/* terminal */
    	names.put("def", new Name("def", p -> t -> {
    		Pinto.State state = p.getState();
    		String name = state.getNameLiteral().orElseThrow(() -> new PintoSyntaxException("def requires a name literal."));
    		String desc = state.getExpression().get();
			Consumer<Table> f = state.getPrevious().andThen(t2 -> {
					t2.collapseFunction();
				});
			p.getNamespace().define(name, state.getNameIndexer(), desc,
					state.getDependencies().subList(0,state.getDependencies().size() - 1), f);
    		t.setStatus("Defined " + name);
    	}, Optional.empty(), Optional.of("[]"), "Defines the expression as the preceding name literal.", true, true, true));

    	names.put("undef", new Name("undef", p -> t -> {
    		String name = p.getState().getNameLiteral().orElseThrow(() -> new PintoSyntaxException("del requires a name literal."));
    		p.getNamespace().undefine(name);
    		t.setStatus("Undefined " + name);
    	},"[]", "Deletes name specified by the preceding name literal.", true));
    	names.put("help", new Name("help", p -> t -> {
    		StringBuilder sb = new StringBuilder();
    		String crlf = System.getProperty("line.separator");
    		if(p.getState().getNameLiteral().isPresent()) {
    			String n = p.getState().getNameLiteral().get();
    			sb.append(p.getNamespace().getName(n).getHelp(n)).append(crlf);
    		} else {
    			sb.append("Pinto help").append(crlf);
    			sb.append("Built-in names:").append(crlf);
    			List<String> l = new ArrayList<>(p.getNamespace().getNames());
    			List<String> l2 = new ArrayList<>();
    			for(int i = 0; i < l.size(); i++) {
    				if(p.getNamespace().getName(l.get(i)).builtIn()) {
    					sb.append(l.get(i)).append(i == l.size()-1 || i > 0 && i % 7 == 0 ? crlf : ", ");
    				} else {
    					l2.add(l.get(i));
    				}
    			}
    			sb.append("Defined names:").append(crlf);
    			for(int i = 0; i < l2.size(); i++) {
    				sb.append(l2.get(i)).append(i == l2.size()-1 || i > 0 && i % 7 == 0 ? crlf : ", ");
    			}
    			sb.append(crlf).append("For help with a specific function type \":function help\"").append(crlf);
    		}
   			t.setStatus(sb.toString());
    	}, "[]", "Prints help for the preceding name literal or all names if one has not been specified.", true));
    	names.put("list", new Name("list", p -> t -> {
    		StringBuilder sb = new StringBuilder();
    		String crlf = System.getProperty("line.separator");
            p.getNamespace().getNames().stream().map(s -> p.getNamespace().getName(s))
                .forEach(name -> {
                    sb.append(name.toString()).append("|").append(name.getIndexString()).append("|")
                        .append(name.getDescription()).append(crlf);
            });
   			t.setStatus(sb.toString());
    	}, "[]", "Shows description for all names.", true));
    	names.put("eval", new Name("eval", p -> t -> {
    		LinkedList<Column<?,?>> s = t.flatten();
    		Periodicity<?> periodicity = ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
    		LinkedList<LocalDate> dates = new LinkedList<>();
    		while((!s.isEmpty()) && dates.size() < 2 && s.peekFirst().getHeader().equals("date")) {
    			dates.add(((Column.OfConstantDates)s.removeFirst()).getValue());
    		}
    		t.evaluate(periodicity.range(dates.removeLast(), dates.isEmpty() ? LocalDate.now() : dates.peek(), false));
    	},"[periodicity=B,date=today today,:]",
    			"Evaluates the expression over date range between two *date* columns over *periodicity*.", true ));
    	names.put("import", new Name("import", p -> t -> {
			for(LinkedList<Column<?,?>> s : t.takeTop()) {
				while(!s.isEmpty()) {
					String filename = ((Column.OfConstantStrings)s.removeFirst()).getValue();
					int lineNumber = 0;
					try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
						String line = null;
						p.engineState.reset();
						while ((line = reader.readLine()) != null) {
							lineNumber++;
							p.eval(line);
						}
					} catch (FileNotFoundException e) {
						throw new IllegalArgumentException("Cannot find pinto file \"" + filename + "\" to execute");
					} catch (IOException e1) {
						throw new IllegalArgumentException("IO error for pinto file \"" + filename + "\" in execute");
					} catch (PintoSyntaxException e) {
						throw new IllegalArgumentException("Pinto syntax error in  file \"" + filename + "\" at line: " + Integer.toString(lineNumber), e);
					}
				}
			}
			t.setStatus("Successfully executed");
    	},"[:]", "Executes pinto expressions contained files specified by file names in string columns.", true));
    	names.put("to_csv", new Name("to_csv", p -> t -> {
    		LinkedList<Column<?,?>> s = t.flatten();
    		String filename = ((Column.OfConstantStrings)s.removeFirst()).getValue();
    		Periodicity<?> periodicity = ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
    		LinkedList<LocalDate> dates = new LinkedList<>();
    		while((!s.isEmpty()) && dates.size() < 2 && s.peekFirst().getHeader().equals("date")) {
    			dates.add(((Column.OfConstantDates)s.removeFirst()).getValue());
    		}
    		t.evaluate(periodicity.range(dates.removeLast(), dates.isEmpty() ? LocalDate.now() : dates.peek(), false));
    		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
    			out.print(t.toCsv());
    		} catch (IOException e) {
    				throw new IllegalArgumentException("Unable to open file \"" + filename + "\" for export");
    		}
			t.setStatus("Successfully exported");
    	},"[filename,periodicity=B,date=today today,:]","Evaluates the expression over the date range " +
    		"specified by *start, *end* and *freq* columns, exporting the resulting table to csv *filename*.", true));


/* stack manipulation */
    	names.put("only", new Name("only", t -> { 
    		List<LinkedList<Column<?,?>>> indexed = t.takeTop();
    		t.clearTop();
    		t.insertAtTop(indexed);
    	},"[:]", "Clears stack except for indexed columns."));
    	names.put("clear", new Name("clear", toTableConsumer(s -> s.clear()),"[:]", "Clears indexed columns from stack."));
    	names.put("rev", new Name("rev", toTableConsumer(s -> Collections.reverse(s)),"[:]","Reverses order of columns in stack."));
    	names.put("copy", new Name("copy", toTableConsumer(s -> {
    		int times = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
    		LinkedList<Column<?,?>> temp = new LinkedList<>();
            s.stream().forEach(temp::addFirst);
            for(int j = 0; j < times - 1; j++) {
            	temp.stream().map(Column::clone).forEach(s::addFirst);
            }
    	}), "[n=2,:]", "Copies indexed columns *n* times."));
    	names.put("roll", new Name("roll", toTableConsumer(s -> {
    		int times = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
    		for(int j = 0; j < times; j++) {
    			s.addFirst(s.removeLast());
    		}
    	}), "[n=1,:]", "Permutes columns in stack *n* times."));
/* dates */
    	names.put("today", new Name("today", toTableConsumer(s -> {
    				s.addFirst(new Column.OfConstantDates(LocalDate.now()));
    	}),"[]", "Creates a constant date column with today's date."));
    	names.put("offset", new Name("offset", toTableConsumer(s -> {
    		LocalDate date = ((Column.OfConstantDates)s.removeFirst()).getValue();
    		Periodicity<?> periodicity = ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
    		int count = ((Column.OfConstantDoubles)s.removeFirst()).getValue().intValue();
    		s.addFirst(new Column.OfConstantDates(periodicity.offset(count, date)));
    	}),"[date=today,periodicity=B,count=-1]", "Offset a given number of periods from today's date."));
    	
/* data creation/testing */
    	/* constants */
    	for(Entry<String, Supplier<Periodicity<?>>> e : Periodicities.map.entrySet()) {
    		names.put(e.getKey(), new Name(e.getKey(), toTableConsumer(s -> {
    				s.addFirst(new Column.OfConstantPeriodicities(e.getValue().get()));
    		}),"[]", "Creates a constant periodcities column for " + e.getKey()));
    	}
    	names.put("pi", new Name("pi", toTableConsumer(s -> {
    				s.addFirst(new Column.OfConstantDoubles(Math.PI));
    	}),"[]", "Creates a constant double column with the value pi."));

    	names.put("moon", new Name("moon", toTableConsumer(s -> {
    		s.addFirst(new Column.OfDoubles(i -> "moon", inputs -> range -> {
    			return range.dates().stream().mapToDouble(d -> {
    				return new tech.pinto.tools.MoonPhase(d).getPhase();
    			});
    		}));
    	}),"[]","Creates a double column with values corresponding the phase of the moon."));
    	names.put("range", new Name("range", toTableConsumer(s -> {
    		int start = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
    		int end = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
    		IntStream.range(start,end).mapToDouble(i -> (double)i).mapToObj(
    				value -> new Column.OfConstantDoubles(value)).forEach(s::addFirst);
    	}),"[start=1,end=4]", "Creates double columns corresponding to integers between *start* (inclusive) and *end* exclusive."));
    	names.put("read_csv", new Name("read_csv", toTableConsumer(s -> {
    		String source = ((Column.OfConstantStrings)s.removeFirst()).getValue();
    		boolean includesHeader = Boolean.parseBoolean(((Column.OfConstantStrings)s.removeFirst()).getValue());
    		try {
    			List<String> lines = null;
    			if(!source.contains("http")) {
    				lines = Files.readAllLines(Paths.get(source));
    			} else {
    				lines = new ArrayList<>();
    		        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(source).openStream()));
    		        String inputLine;
    		        while ((inputLine = in.readLine()) != null)
    		            lines.add(inputLine);
    		        in.close();
    			}
    			if (lines.size() > 0) {
    				String[] firstRow = lines.get(0).split(",");
    				final String[] labels = includesHeader ? Arrays.copyOfRange(firstRow, 1, firstRow.length) :
    					new String[firstRow.length];
    				final Map<LocalDate, String[]> data = lines.stream().skip(includesHeader ? 1 : 0).map(line -> line.split(","))
    						.collect(Collectors.toMap((r) -> LocalDate.parse(r[0]), Function.identity()));
    				for(int i = 0; i < firstRow.length - 1; i++) {
    					final int col = i;
    					s.add(new Column.OfDoubles(inputs -> labels[col] != null ? labels[col] : "", 
    							inputs -> range -> {
    							DoubleStream.Builder b = DoubleStream.builder();
    							for (Period per : range.values()) {
    								if (data.containsKey(per.endDate())) {
    									try {
    										b.accept(Double.valueOf(data.get(per.endDate())[col + 1]));
    									} catch(NumberFormatException nfe) {
    										b.accept(Double.NaN);
    									}
    								} else {
    									b.accept(Double.NaN);
    								}
    							}
    							return b.build();
    								
    					}));
    				}
    			}
    		} catch (IOException e) {
    			throw new PintoSyntaxException("Unable to import file \"" + source + "\".", e);
    		}	
    	}),"[source,includes_header=\"true\"]", "Reads CSV formatted table from file or URL specified as *source*."));


/* data cleanup */
    	names.put("fill", new Name("fill", toTableConsumer(s-> {
			@SuppressWarnings("rawtypes")
			Periodicity p = ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
    		boolean lookBack = Boolean.parseBoolean(((Column.OfConstantStrings)s.removeFirst()).getValue());
    		s.replaceAll(function -> {
    			return new Column.OfDoubles(inputs -> inputs[0].toString() + " fill", inputs -> range -> {
    				DoubleStream input = null;
    				int skip = 0;
    				PeriodicRange<Period> r = (PeriodicRange<Period>) range;
    				if (lookBack) {
						r = (PeriodicRange<Period>) range.periodicity().range(
    							p.previous(p.from(range.start().endDate())).endDate(),
    							range.end().endDate(), range.clearCache());
    					skip = (int) r.indexOf(range.start());
    				} 
   					input = ((Column.OfDoubles)inputs[0]).rows(r);
    				final AtomicReference<Double> lastGoodValue = new AtomicReference<>(Double.NaN);
    				return input.map(d -> {
    					if (!Double.isNaN(d)) {
    						lastGoodValue.set(d);
    					}
    					return lastGoodValue.get();
    				}).skip(skip);
    			}, new Column<?,?>[] {function});
    		});
    	}),"[periodicity=BQ-DEC,lookback=\"true\",:]","Fills missing values with last good value, " +
    			"looking back one period of *freq* if *lookback* is true."));
    	names.put("join", new Name("join", toTableConsumer(s-> {
    		LinkedList<LocalDate> cutoverDates = new LinkedList<>();
    		while((!s.isEmpty()) && s.peekFirst().getHeader().equals("date")) {
    			cutoverDates.add(((Column.OfConstantDates)s.removeFirst()).getValue());
    		}
    		Column.OfDoubles[] inputStack = s.toArray(new Column.OfDoubles[] {});
    		s.clear();
    		s.add(new Column.OfDoubles(i -> "join", inputArray -> range -> {
    			LinkedList<Column<?,?>> inputs = new LinkedList<>(Arrays.asList(inputArray));
    			Collections.reverse(inputs);
    			DoubleStream ds = DoubleStream.empty().sequential();
    			List<Period> cutoverPeriods = cutoverDates.stream().map(range.periodicity()::from).collect(Collectors.toList());
    			Collections.sort(cutoverPeriods);
    			Period current = range.start();
    			Periodicity<Period> freq = (Periodicity<Period>) range.periodicity();
    			int i = 0;
    			while (i < cutoverPeriods.size() && !current.isAfter(range.end())) {
    				if(inputs.isEmpty()) {
    					throw new PintoSyntaxException("Not enough columns to join on " + cutoverDates.size() + " dates.");
    				}
    				Column.OfDoubles currentFunction = (Column.OfDoubles) inputs.removeFirst();
    				if (current.isBefore(cutoverPeriods.get(i))) {
    					Period chunkEnd = range.end().isBefore(cutoverPeriods.get(i)) ? range.end()
    							: freq.previous(cutoverPeriods.get(i));
    					ds = DoubleStream.concat(ds, currentFunction.rows(freq.range(current, chunkEnd, range.clearCache())));
    					current = freq.next(chunkEnd);
    				}
    				i++;
    			}
    			if (inputs.isEmpty()) {
    				throw new IllegalArgumentException("Not enough columns to join on " + cutoverDates.size() + " dates.");
    			}
    			Column.OfDoubles currentFunction = (Column.OfDoubles) inputs.removeFirst();
    			if (!current.isAfter(range.end())) {
    				ds = DoubleStream.concat(ds, currentFunction.rows(range.periodicity().range(current, range.end(), range.clearCache())));
    			}
    			return ds;
    		}, inputStack)); 
    	}),"[date,:]", "Joins columns over time, switching between columns on dates supplied in \";\" denominated list *dates*."));
    	names.put("resample", new Name("resample", toTableConsumer(s-> {
			Periodicity<Period> newPeriodicity = Periodicities.get(((Column.OfConstantStrings)s.removeFirst()).getValue());
			s.replaceAll(c -> {
				return new Column.OfDoubles(inputs -> inputs[0].toString() + " resample",  inputs -> range -> {
					Period newStart = newPeriodicity.roundDown(range.start().endDate());
					if(newStart.endDate().isAfter(range.start().endDate())) {
						newStart = newStart.previous();
					}
					Period newEnd = newPeriodicity.from(range.end().endDate());
					PeriodicRange<Period> newDr = newPeriodicity.range(newStart, newEnd, range.clearCache());
					double[] d = ((DoubleStream) inputs[0].rows(newDr)).toArray();
					DoubleStream.Builder b = DoubleStream.builder();
					range.values().stream().map(Period::endDate).forEach( ed -> {
							b.accept(d[(int) newDr.indexOf(newPeriodicity.roundDown(ed))]);
					});
					return b.build();
				}, c);
			});
    	}),"[freq=\"BM\",:]", "Sets frequency of prior columns to periodicity *freq*, carrying values forward " +
			"if evaluation periodicity is more frequent."));
    	names.put("hformat", new Name("hformat", toTableConsumer(s-> {
    		MessageFormat format = new MessageFormat(((Column.OfConstantStrings)s.removeFirst()).getValue().replaceAll("\\{\\}", "\\{0\\}"));
    		s.replaceAll(c -> {
    			String header = format.format(new Object[]{c.getHeader()});
    			c.setHeaderFunction(inputs -> header);
    			return c;
    		});

    	}),"[format,:]", "Formats headers, setting new value to *format* and substituting and occurences of \"{}\" with previous header value."));
    	
/* array creation */
    	names.put("rolling", new Name("rolling", toTableConsumer(s-> {
    		int size = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
            String wfs = ((Column.OfConstantStrings)s.removeFirst()).getValue();
            Optional<Periodicity<Period>> windowFreq = wfs.equals("eval") ? Optional.empty() : Optional.of(Periodicities.get(wfs));
    		s.replaceAll(c -> {
    			return new Column.OfDoubleArray1Ds(inputs -> inputs[0].getHeader() + " rolling", inputs -> range -> {
                    Periodicity<Period> wf = windowFreq.orElse((Periodicity<Period>) range.periodicity());
    				Stream.Builder<DoubleStream> b = Stream.builder();
    				Period expandedWindowStart = wf.offset(-1 * (size - 1), wf.from(range.start().endDate()));
    				Period windowEnd = wf.from(range.end().endDate());
    				PeriodicRange<Period> expandedWindow = wf.range(expandedWindowStart, windowEnd, range.clearCache());
    				double[] data = ((Column.OfDoubles)inputs[0]).rows(expandedWindow).toArray();
    				for(Period p : range.values()) {
    					long windowStartIndex = wf.distance(expandedWindowStart, wf.from(p.endDate())) - size + 1;
    					b.accept(Arrays.stream(data, (int) windowStartIndex, (int) windowStartIndex + size));
    				}
    				return b.build();
    			},size,c);
    		});
    	}),"[size=2,freq=\"eval\",:]", "Creates double array columns for each input column with rows containing values "+
    			"from rolling window of past data where the window is *size* periods of periodicity *freq*, defaulting to the evaluation periodicity."));
    	names.put("cross", new Name("cross", toTableConsumer(s-> {
    		Column.OfDoubles[] a = s.toArray(new Column.OfDoubles[]{});
    		s.clear();
    		s.add(new Column.OfDoubleArray1Ds(inputs -> "cross", inputs -> range -> {
				Stream.Builder<DoubleStream> b = Stream.builder();
				List<OfDouble> l = Stream.of(inputs).map(c -> ((Column.OfDoubles) c).rows(range))
						.map(DoubleStream::iterator).collect(Collectors.toList());
    			for(int i = 0; i < range.size(); i++) {
    				DoubleStream.Builder db = DoubleStream.builder();
    				l.stream().mapToDouble(OfDouble::nextDouble).forEach(db::accept);
    				b.accept(db.build());
    			}
    			return b.build();
    		}, i -> Optional.of(new int[]{i.length}),a));
    	}),"[:]", "Creates a double array column with each row containing values of input columns."));
    	names.put("expanding", new Name("expanding", toTableConsumer(s-> {
    		Column<?,?> col = s.removeFirst();
    		Optional<LocalDate> start = col instanceof Column.OfConstantDates ? Optional.of(((Column.OfConstantDates)col).getValue()) : Optional.empty();
    		col = s.removeFirst();
    		Optional<Periodicity<?>> periodicity = col instanceof Column.OfConstantPeriodicities ? Optional.of(((Column.OfConstantPeriodicities) col).getValue()) : Optional.empty();
    		boolean initialZero = Boolean.parseBoolean(((Column.OfConstantStrings)s.removeFirst()).getValue());
    		s.replaceAll(c -> {
    			return new Column.OfDoubleArray1Ds(inputs -> inputs[0].getHeader() + " expanding", inputs -> range -> {
    				LocalDate startDate = start.orElse(range.start().endDate());
    				Periodicity<Period> wf = (Periodicity<Period>) periodicity.orElse(range.periodicity());
    				Period windowStart = wf.from(startDate);
    				Period windowEnd = wf.from(range.end().endDate());
    				windowEnd = windowEnd.isBefore(windowStart) ? windowStart : windowEnd;
    				PeriodicRange<Period> window = wf.range(windowStart, windowEnd, range.clearCache());
    				Stream.Builder<DoubleStream> b = Stream.builder();
    				double[] data = ((Column.OfDoubles)inputs[0]).rows(window).toArray();
    				if(initialZero) {
    					data[0] = 0.0d;
    				}
    				List<LocalDate> rangeDates = range.dates();
    				for(int i = 0; i < range.size(); i++) {
    					int index = (int) window.indexOf(rangeDates.get(i));
    					if (index >= 0) {
    						b.accept(Arrays.stream(data, 0, index + 1));
    					} else {
    						b.accept(DoubleStream.iterate(Double.NaN, r -> Double.NaN).limit(range.size() + 1));
    					}
    				}
    				return b.build();
    			},c);
    		});
    	}),"[date=\"range\",periodicity=\"range\",initial_zero=\"false\",:]", "Creates double array columns for each input column with rows " +
    		"containing values from an expanding window of past data with periodicity *freq* that starts on date *start*."));

/* array operators */
    	for(DoubleCollectors dc : DoubleCollectors.values()) {
    		names.put(dc.name(), new Name(dc.name(), makeOperator(dc.name(), dc),"[:]","Aggregates row values in double array " +
    				"columns to a double value by " + dc.name()));
    	}
//    	names.put("rank", new Name("rank", toTableConsumer(s-> {
//    		s.replaceAll(c -> {
//    			return new Column.OfDoubleArray1Ds(inputs -> inputs[0].toString() + " rank",
//    				inputs -> range -> {
//    					Column.OfDoubleArray1Ds col = (Column.OfDoubleArray1Ds) inputs[0];
//    					col.setRange(range);
//    					return col.rows().map(ds -> {
//    						double[] d = ds.toArray();
//    						Double[] ranks = new Double[d.length];
//    						for(int i = 0; i < d.length; i++) {
//    							ranks[i] = (double) i;
//    						}
//    						Arrays.sort(ranks, (i0,i1) -> (int) Math.signum(d[i0.intValue()] - d[i1.intValue()]));
//    						return Arrays.stream(ranks).mapToDouble(Double::doubleValue);
//    					});
//    				}, c);
//    		});
//    	}),"[:]", "Creates a double array column with each row containing values of input columns."));

/* binary operators */
    	names.put("+", makeOperator("+", (x,y) -> x + y));
    	names.put("-", makeOperator("-", (x,y) -> x - y));
    	names.put("*", makeOperator("*", (x,y) -> x * y));
    	names.put("/", makeOperator("/", (x,y) -> x / y));
    	names.put("%", makeOperator("%", (x,y) -> x % y));
    	names.put("^", makeOperator("^", (x,y) -> Math.pow(x, y)));
    	names.put("==", makeOperator("==", (x,y) -> x == y ? 1.0 : 0.0));
    	names.put("!=", makeOperator("!=", (x,y) -> x != y ? 1.0 : 0.0));
    	names.put(">", makeOperator(">", (x,y) -> x > y ? 1.0 : 0.0));
    	names.put("<", makeOperator("<", (x,y) -> x < y ? 1.0 : 0.0));
    	names.put(">=", makeOperator(">=", (x,y) -> x >= y ? 1.0 : 0.0));
    	names.put("<=", makeOperator("<=", (x,y) -> x <= y ? 1.0 : 0.0));


/* unary operators */
    	names.put("abs", makeOperator("abs", (DoubleUnaryOperator) Math::abs));
    	names.put("sin", makeOperator("sin", Math::sin));
    	names.put("cos", makeOperator("cos", Math::cos));
    	names.put("tan", makeOperator("tan", Math::tan));
    	names.put("sqrt",makeOperator("sqrt", Math::sqrt));
    	names.put("log", makeOperator("log", Math::log));
    	names.put("log10", makeOperator("log10", Math::log10));
    	names.put("exp", makeOperator("exp", Math::exp));
    	names.put("signum", makeOperator("signum", (DoubleUnaryOperator)Math::signum));
    	names.put("asin", makeOperator("asin", Math::asin));
    	names.put("acos", makeOperator("acos", Math::acos));
    	names.put("atan", makeOperator("atan", Math::atan));
    	names.put("toRadians", makeOperator("toRadians", Math::toRadians));
    	names.put("toDegrees", makeOperator("toDegrees", Math::toDegrees));
    	names.put("cbrt", makeOperator("cbrt", Math::cbrt));
    	names.put("ceil", makeOperator("ceil", Math::ceil));
    	names.put("floor", makeOperator("floor", Math::floor));
    	names.put("rint", makeOperator("rint", Math::rint));
    	names.put("ulp", makeOperator("ulp", (DoubleUnaryOperator)Math::ulp));
    	names.put("sinh", makeOperator("sinh", Math::sinh));
    	names.put("cosh", makeOperator("cosh", Math::cosh));
    	names.put("tanh", makeOperator("tanh", Math::tanh));
    	names.put("expm1", makeOperator("expm1", Math::expm1));
    	names.put("log1p", makeOperator("log1p", Math::log1p));
    	names.put("nextUp", makeOperator("nextUp", (DoubleUnaryOperator)Math::nextUp));
    	names.put("nextDown", makeOperator("nextDown", (DoubleUnaryOperator)Math::nextDown));
    	names.put("neg", makeOperator("neg", x -> x * -1.0d));
    	names.put("inv", makeOperator("inv", x -> 1.0 / x));
    	names.put("acgbPrice", makeOperator("acgbPrice", quote -> {
    		double TERM = 10, RATE = 6, price = 0; 
    		for (int i = 0; i < TERM * 2; i++) {
    			price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
    		}
    		return price + 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
    	}));
		names.put("report", new Name("rpt", p -> t -> {
			Pinto.State state = p.getState();
			String id = getId();
			p.getNamespace().define("_rpt-" + id, state.getNameIndexer(), "Report id: " + id,
					state.getDependencies().subList(0, state.getDependencies().size() - 1), state.getPrevious());
			try {
				int port = p.getPort();
				Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + port + "/pinto/report?p=" + id));
			} catch (Exception e) {
				throw new PintoSyntaxException("Unable to open report", e);
			}
			t.setStatus("Report id: " + id);
		}, Optional.empty(), Optional.of("[HTML]"), "Creates an HTML page from any columns labelled HTML.", true, true,
				true));
		names.put("grid", new Name("grid", p -> toTableConsumer(s -> {
			int columns = Double.valueOf(((Column.OfConstantDoubles) s.removeFirst()).getValue()).intValue();
			StringBuilder sb = new StringBuilder();
			sb.append("\n<table class=\"pintoGrid\">\n\t<tbody>\n");
			for (int i = 0; i < s.size();) {
				if (i % columns == 0) {
					sb.append("\t\t<tr>\n");
				}
				sb.append("\t\t\t<td>").append(((Column.OfConstantStrings) s.get(s.size() - i++ - 1)).getValue())
						.append("</td>\n");
				if (i % columns == 0 || i == s.size()) {
					sb.append("\t\t</tr>\n");
				}
			}
			sb.append("\t</tbody>\n</table>\n");
			s.clear();
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}), "[columns=3,HTML]",
				"Creates a grid layout in a report with all input columns labelled HTML as cells in the grid.", false));
		names.put("table", new Name("table", p -> toTableConsumer(s -> {
			Periodicity<?> periodicity = ((Column.OfConstantPeriodicities) s.removeFirst()).getValue();
			LinkedList<LocalDate> d = new LinkedList<>();
			while ((!s.isEmpty()) && d.size() < 2 && s.peekFirst().getHeader().equals("date")) {
				d.add(((Column.OfConstantDates) s.removeFirst()).getValue());
			}
			PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek(), false);
			NumberFormat nf = ((Column.OfConstantStrings) s.removeFirst()).getValue().equals("percent")
					? NumberFormat.getPercentInstance()
					: NumberFormat.getNumberInstance();
			nf.setGroupingUsed(false);
			Table t = new Table();
			t.insertAtTop(s);
			t.evaluate(range);
			String[] lines = t.toCsv(nf).split("\n");
			s.clear();
			StringBuilder sb = new StringBuilder();
			sb.append("<table class=\"pintoTable\">\n<thead>\n");
			Arrays.stream(lines[0].split(","))
					.forEach(h -> sb.append("\t<th class=\"rankingTableHeader\">").append(h).append("</th>\n"));
			sb.append("</thead>\n<tbody>\n");
			Arrays.stream(lines).skip(1).map(l -> l.split(",")).forEach(l -> {
				sb.append("<tr>\n");
				Arrays.stream(l).forEach(c -> sb.append("<td>").append(c).append("</td>"));
				sb.append("</tr>\n");
			});
			sb.append("</tbody></table>\n");
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}), "[periodicity=B, date=[count=-20] offset today,format=\"decimal\",:]",
				"Creates a const string column with code for an HTML ranking table.", false));
		names.put("chart", new Name("chart", p -> toTableConsumer(s -> {
			Periodicity<?> periodicity = ((Column.OfConstantPeriodicities) s.removeFirst()).getValue();
			LinkedList<LocalDate> d = new LinkedList<>();
			while ((!s.isEmpty()) && d.size() < 2 && s.peekFirst().getHeader().equals("date")) {
				d.add(((Column.OfConstantDates) s.removeFirst()).getValue());
			}
			PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek(), false);
			String title = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			if (!chartHTML.isPresent()) {
				try {
					chartHTML = Optional.of(new MessageFormat(readInputStreamIntoString(
							(getClass().getClassLoader().getResourceAsStream("report_chart.html")))));
				} catch (IOException e) {
					throw new PintoSyntaxException("Unable to open chart html template", e);
				}
			}
			String dates = range.dates().stream().map(LocalDate::toString)
					.collect(Collectors.joining("', '", "['x', '", "']"));
			StringBuilder data = new StringBuilder();
			for (int i = s.size() - 1; i >= 0; i--) {
				data.append("['").append(s.get(i).getHeader()).append("',");
				data.append(((Column.OfDoubles) s.get(i)).rows(range).mapToObj(String::valueOf)
						.collect(Collectors.joining(", ", "", "]")));
				data.append(i > 0 ? "," : "");
			}
			String html = chartHTML.get().format(new Object[] { getId(), dates, data, title }, new StringBuffer(), null)
					.toString();
			s.clear();
			s.add(new Column.OfConstantStrings(html, "HTML"));
		}), "[periodicity=B, date=[count=-20] offset today,title=\"\",:]",
				"Creates a const string column with code for an HTML chart.", false));
		names.put("rt", new Name("rt", p -> toTableConsumer(s -> {
			LinkedList<LocalDate> starts = new LinkedList<>();
			while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("starts")) {
				starts.addFirst(((Column.OfConstantDates) s.removeFirst()).getValue());
			}
			LinkedList<LocalDate> ends = new LinkedList<>();
			while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("ends")) {
				ends.addFirst(((Column.OfConstantDates) s.removeFirst()).getValue());
			}
			LinkedList<String> columnLabels = new LinkedList<>();
			while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("labels")) {
				columnLabels.addFirst(((Column.OfConstantStrings) s.removeFirst()).getValue());
			}
			LinkedList<Periodicity<?>> periodicities = new LinkedList<>();
			while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("periodicities")) {
				periodicities.addFirst(((Column.OfConstantPeriodicities) s.removeFirst()).getValue());
			}
			LinkedList<DoubleCollectors> collectors = new LinkedList<>();
			while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("collectors")) {
				collectors.addFirst(DoubleCollectors.valueOf(((Column.OfConstantStrings) s.removeFirst()).getValue()));
			}
			DoubleCollectors[] dc = Arrays.stream(((Column.OfConstantStrings) s.removeFirst()).getValue().split(","))
					.map(String::trim).map(str -> DoubleCollectors.valueOf(str)).toArray(DoubleCollectors[]::new);
			NumberFormat nf = ((Column.OfConstantStrings) s.removeFirst()).getValue().equals("percent")
					? NumberFormat.getPercentInstance()
					: NumberFormat.getNumberInstance();
			int digits = ((Column.OfConstantDoubles) s.removeFirst()).getValue().intValue();
			nf.setMaximumFractionDigits(digits);
			int columns = starts.size();
			int rows = s.size();
			String[] labels = new String[rows];
			String[] headers = new String[columns];
			String[][] cells = new String[rows][columns];
			for (int i = 0; i < columns; i++) {
				LocalDate start = starts.get(i);
				LocalDate end = i < ends.size() ? ends.get(i) : LocalDate.now();
				PeriodicRange<?> pr = periodicities.get(Math.min(i, periodicities.size() - 1)).range(start, end, false);
				String columnLabel = i >= columnLabels.size() ? "" : columnLabels.get(i);
				headers[i] = columnLabel.equals("") ? start + " - " + end : columnLabel;
				List<Column<?, ?>> l = null;
				if (i == columns - 1) {
					l = new ArrayList<>(s);
					s.clear();
				} else {
					l = new ArrayList<>();
					s.stream().map(Column::clone).forEach(l::add);
				}
				double[][] values = new double[l.size()][2];
				for (int j = 0; j < l.size(); j++) {
					if (i == 0) {
						labels[j] = l.get(j).getHeader();
					}
					values[j][0] = ((Column.OfDoubles) l.get(j)).rows(pr)
							.collect(dc[Math.min(i, dc.length - 1)], (v, d) -> v.add(d), (v, v1) -> v.combine(v1))
							.finish();
					values[j][1] = (int) j;
				}
				Arrays.sort(values, (c1, c2) -> c1[0] == c2[0] ? 0 : c1[0] < c2[0] ? 1 : -1);
				for (int j = 0; j < values.length; j++) {
					StringBuilder sb = new StringBuilder();
					sb.append("\t<td id=\"rankingColor").append((int) values[j][1])
							.append("\" class=\"rankingTableCell\">").append(labels[(int) values[j][1]]).append(": ")
							.append(nf.format(values[j][0])).append("</td>\n");
					cells[j][i] = sb.toString();
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("<table class=\"rankingTable\">\n<thead>\n");
			Arrays.stream(headers)
					.forEach(h -> sb.append("\t<th class=\"rankingTableHeader\">").append(h).append("</th>\n"));
			sb.append("</thead>\n<tbody>\n");
			for (int i = 0; i < cells.length; i++) {
				sb.append("<tr>\n");
				for (int j = 0; j < columns; j++) {
					sb.append(cells[i][j]);
				}
				sb.append("</tr>\n");
			}
			sb.append("</tbody></table>\n");
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}), "[starts=[periodicity=BA-DEC] offset,ends=today,labels=\"YTD\",periodicities=B,functions=\"pct_change\",format=\"percent\",digits=2,:]",
				"Creates a const string column with code for an HTML ranking table.", false));
    	
    }

	@Override
	protected Map<String, Name> getNameMap() {
		return names;
	}
	

//	private static Name makePintoName(String name, String pintoCode, String defaultIndexer, String description) {
//		return new Name(name, p -> t ->  {
//			Pinto.State state = new Pinto.State(false);
//			p.evaluate(pintoCode, state);
//			state.getCurrent().accept(t);
//		}, defaultIndexer, description, false);
//	}

	private static Name makeOperator(String name, DoubleBinaryOperator dbo) {
		return new Name(name, toTableConsumer(stack -> {
			int rightCount = (int) ((Column.OfConstantDoubles) stack.removeFirst()).getValue().doubleValue();
			if (stack.size() < rightCount + 1) {
				throw new IllegalArgumentException("Not enough inputs for " + name);
			}
			LinkedList<Column<?,?>> rights = new LinkedList<>(stack.subList(0, rightCount));
			List<Column<?,?>> lefts = new ArrayList<>(stack.subList(rightCount, stack.size()));
			stack.clear();
			for (int i = 0; i < lefts.size(); i++) {
				Column<?,?> right = i >= rights.size() ? rights.getFirst().clone() : rights.getFirst();
				rights.addLast(rights.removeFirst());
				if(right instanceof Column.OfDoubles && lefts.get(i) instanceof Column.OfDoubles) {
					stack.add(new Column.OfDoubles(inputs -> inputs[1].toString() + " " + inputs[0].toString() + " " + name,
						inputs -> range -> {
							OfDouble leftIterator = ((Column.OfDoubles)inputs[1]).rows(range).iterator();
							return  ((Column.OfDoubles)inputs[0]).rows(range).map(r -> dbo.applyAsDouble(leftIterator.nextDouble(), r));
						}, right, lefts.get(i)));
				} else if(right instanceof Column.OfDoubleArray1Ds && lefts.get(i) instanceof Column.OfDoubleArray1Ds) {
					stack.add(new Column.OfDoubleArray1Ds(inputs -> inputs[1].toString() + " " + inputs[0].toString() + " " + name,
						inputs -> range -> {
							Iterator<DoubleStream> leftIterator = ((Column.OfDoubleArray1Ds)inputs[1]).rows(range).iterator();
							return  ((Column.OfDoubleArray1Ds)inputs[0]).rows(range)
									.map(strm -> {
										OfDouble leftDoubleIterator = leftIterator.next().iterator();
										return strm.map(r -> dbo.applyAsDouble(leftDoubleIterator.nextDouble(), r));
									});
						}, right, lefts.get(i)));
				} else if(right instanceof Column.OfDoubles && lefts.get(i) instanceof Column.OfDoubleArray1Ds) {
					stack.add(new Column.OfDoubleArray1Ds(inputs -> inputs[1].toString() + " " + inputs[0].toString() + " " + name,
							inputs -> range -> {
								OfDouble leftIterator = ((Column.OfDoubles)inputs[0]).rows(range).iterator();
								return  ((Column.OfDoubleArray1Ds)inputs[1]).rows(range).map(rstrm -> {
									double l = leftIterator.nextDouble();
									return rstrm.map(r -> dbo.applyAsDouble(r, l));
								});
						}, right, lefts.get(i)));
				} else if(right instanceof Column.OfDoubleArray1Ds && lefts.get(i) instanceof Column.OfDoubles) {
					stack.add(new Column.OfDoubleArray1Ds(inputs -> inputs[1].toString() + " " + inputs[0].toString() + " " + name,
						inputs -> range -> {
							OfDouble leftIterator = ((Column.OfDoubles)inputs[1]).rows(range).iterator();
							return  ((Column.OfDoubleArray1Ds)inputs[0]).rows(range).map(rstrm -> {
								double l = leftIterator.nextDouble();
								return rstrm.map(r -> dbo.applyAsDouble(l, r));
							});
						}, right, lefts.get(i)));
				} else {
					throw new IllegalArgumentException("Operator " + dbo.toString() + " can only operate on columns of doubles or double arrays.");
				}
			}
		}),"[n=1,:]", "Binary double operator " + name + " that operates on *n* columns at a time with fixed right-side operand.");
	}

	private static Name makeOperator(String name, DoubleUnaryOperator duo) {
		return new Name(name, toTableConsumer(stack -> {
			stack.replaceAll(c -> {
				if(c instanceof Column.OfDoubles) {
					return new Column.OfDoubles(inputs -> inputs[0].toString() + " " + name,
							inputs -> range -> ((Column.OfDoubles)inputs[0]).rows(range).map(duo), c);
				} else if(c instanceof Column.OfDoubleArray1Ds) {
					return new Column.OfDoubleArray1Ds(inputs -> inputs[0].toString() + " " + name,
							inputs -> range -> ((Column.OfDoubleArray1Ds)inputs[0]).rows(range)
								.map(strm -> strm.map(duo)), c);
				} else {
					throw new IllegalArgumentException("Operator " + duo.toString() + " can only operate on columns of doubles or double arrays.");
				}
			});
		}),"[:]", "Unary double operator " + name);
	}

	private static Consumer<Table> makeOperator(String name, Supplier<DoubleCollector> dc) {
		return Pinto.toTableConsumer(stack -> {
			stack.replaceAll(c -> {
				return new Column.OfDoubles(inputs -> Stream.of(inputs[0].toString(), name).collect(Collectors.joining(" ")),
					inputs -> range -> {
						if(!(inputs[0] instanceof Column.OfDoubleArray1Ds)) {
							throw new IllegalArgumentException("Operator " + dc.toString() + " can only operate on columns of double arrays.");
						}
						return ((Column.OfDoubleArray1Ds)inputs[0]).rows(range).mapToDouble( s -> {
							return s.collect(dc, (v,d) -> v.add(d), (v,v1) -> v.combine(v1)).finish();
						});
				}, c);
			});
		});
	}
	
	private static String getId() {
		String id = null;
		do {
			id = UUID.randomUUID().toString().replaceAll("\\d", "").replaceAll("-", "");
		} while (id.length() < 8);
		return id.substring(0, 8);
	}

	private static String readInputStreamIntoString(InputStream inputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while (result != -1) {
			buf.write((byte) result);
			result = bis.read();
		}
		return buf.toString("UTF-8");
	}

	

}
