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
import java.util.concurrent.atomic.AtomicInteger;
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

import tech.pinto.Column.ConstantColumn;
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
    	names.put("clear", new Name("clear", toTableConsumer(s -> s.clear()),"[:]", "Clears indexed columns from table."));
    	names.put("rev", new Name("rev", toTableConsumer(s -> Collections.reverse(s)),"[:]","Reverses order of input columns."));
    	names.put("pull", new Name("pull", toTableConsumer(s -> {return;}),"[:]","Brings input columns to the front."));
    	names.put("copy", new Name("copy", toTableConsumer(s -> {
    		int times = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
    		LinkedList<Column<?,?>> temp = new LinkedList<>();
            s.stream().forEach(temp::addFirst);
            for(int j = 0; j < times - 1; j++) {
            	temp.stream().map(Column::clone).forEach(s::addFirst);
            }
    	}), "[c=2,:]", "Copies indexed columns *c* times."));
    	names.put("roll", new Name("roll", toTableConsumer(s -> {
    		int times = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
    		for(int j = 0; j < times; j++) {
    			s.addLast(s.removeFirst());
    		}
    	}), "[c=1,:]", "Permutes columns in stack *c* times."));
    	names.put("columns", new Name("columns", toTableConsumer(s -> {
    				s.addFirst(new Column.OfConstantDoubles(s.size()));
    	}),"[:]", "Adds a constant double column with the number of input columns."));
    	names.put("index", new Name("index", p -> t -> {
    		LinkedList<Column<?,?>> s = t.takeTop().get(0);
    		List<String> endpoints = new LinkedList<>();
    		while(!s.isEmpty() && endpoints.size() < 2 && s.peekFirst().getHeader().equals("c")) {
    			endpoints.add(Integer.toString((int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue()));
    		}
    		t.insertAtTop(s);
    		String index = endpoints.size() == 1 ? ":" + endpoints.get(0) :
    			endpoints.get(1) + ":" + endpoints.get(0);
    		new Indexer(p, index, false).accept(t);
    	},"[c]", "Indexes by constant double ordinals in c (start inclusive, end exclusive).  Assumes 0 for start if c is one constant.", false));
    	
    	
/* dates */
    	names.put("today", new Name("today", toTableConsumer(s -> {
    				s.addFirst(new Column.OfConstantDates(LocalDate.now()));
    	}),"[]", "Creates a constant date column with today's date."));
    	names.put("offset", new Name("offset", toTableConsumer(s -> {
    		LocalDate date = ((Column.OfConstantDates)s.removeFirst()).getValue();
    		Periodicity<?> periodicity = ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
    		int count = ((Column.OfConstantDoubles)s.removeFirst()).getValue().intValue();
    		s.addFirst(new Column.OfConstantDates(periodicity.offset(count, date)));
    	}),"[date=today,periodicity=B,c=-1]", "Offset a *c* periods of *periodicity* from *date*."));
    	names.put("day_count", new Name("day_count", toTableConsumer(s -> {
    		s.addFirst(new Column.OfDoubles(c -> "day_count", c -> range -> range.values().stream().mapToDouble(Period::dayCount)));
    	}),"[]", "Creates a column with count of days in each period."));
    	names.put("annualization_factor", new Name("annualization_factor", toTableConsumer(s -> {
            Optional<Periodicity<Period>> periodicity = s.peekFirst() instanceof Column.OfConstantStrings &&
            		((Column.OfConstantStrings)s.removeFirst()).getValue().equals("range") ? Optional.empty() :
            			Optional.of((Periodicity<Period>)((Column.OfConstantPeriodicities)s.removeFirst()).getValue());
    		s.addFirst(new Column.OfDoubles(c -> "annualization_factor", c -> range ->
    					DoubleStream.iterate(periodicity.orElse((Periodicity<Period>) range.periodicity()).annualizationFactor(),
    							r -> range.periodicity().annualizationFactor()).limit(range.size())));
    	}),"[periodicity=\"range\"]", "Creates a column with annualization factor for the specified or evaluated range's periodicity."));
    	

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
    		LinkedList<Integer> endpoints = new LinkedList<Integer>();
    		while(!s.isEmpty() && endpoints.size() < 2 && s.peekFirst().getHeader().equals("c")) {
    			endpoints.addLast((int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue());
    		}
    		int start = endpoints.size() == 2 ? endpoints.removeLast() : 1;
    		int end = endpoints.removeFirst();
    		IntStream.range(start,end).mapToDouble(i -> (double)i).mapToObj(
    				value -> new Column.OfConstantDoubles(value)).forEach(s::addFirst);
    	}),"[c=1 4]", "Creates double columns corresponding to integers between first (inclusive) and second (exclusive) in *c*."));
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
    			return new Column.OfDoubles(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " fill", inputs -> range -> {
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
    		s.add(new Column.OfDoubles(i -> i[0].getHeader(), i -> Arrays.stream(i).map(Column::getTrace).collect(Collectors.joining(" ")) + " join",
    		inputArray -> range -> {
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
			Periodicity<Period> newPeriodicity = (Periodicity<Period>) ((Column.OfConstantPeriodicities)s.removeFirst()).getValue();
			s.replaceAll(c -> {
				return new Column.OfDoubles(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + newPeriodicity.code() + " resample",
					inputs -> range -> {
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
    	}),"[periodicity=BM,:]", "Sets frequency of prior columns to periodicity *freq*, carrying values forward " +
			"if evaluation periodicity is more frequent."));
    	
    	
/* header manipulation */
    	names.put("hcopy", new Name("hcopy", toTableConsumer(s-> {
    		List<String> headers = s.stream().map(Column::getHeader).collect(Collectors.toList());
    		Collections.reverse(headers);
    		s.addFirst(new Column.OfConstantStrings(headers.stream().collect(Collectors.joining(","))));
    	}),"[:]", "Copies headers to a comma-delimited constant string column."));
    	names.put("hpaste", new Name("hpaste", toTableConsumer(s-> {
    		String[] headers = ((Column.OfConstantStrings)s.removeFirst()).getValue().split(",");
    		boolean repeat = Boolean.parseBoolean(((Column.OfConstantStrings)s.removeFirst()).getValue());
    		AtomicInteger i = new AtomicInteger(headers.length-1);
    		s.replaceAll(c -> {
    			int index = i.getAndDecrement();
    			if(index >= 0 || repeat) {
    				c.setHeader(headers[index <= 0 ? 0 : index]);
    			}
    			return c;
    		});
    	}),"[string,repeat=\"true\",:]", "Sets headers of other input columns to the values in comma-delimited constant string column."));
    	names.put("hformat", new Name("hformat", toTableConsumer(s-> {
    		MessageFormat format = new MessageFormat(((Column.OfConstantStrings)s.removeFirst()).getValue().replaceAll("\\{\\}", "\\{0\\}"));
    		s.replaceAll(c -> {
    			String header = format.format(new Object[]{c.getHeader()});
    			c.setHeader(header);
    			return c;
    		});

    	}),"[string,:]", "Formats headers, setting new value to *format* and substituting and occurences of \"{}\" with previous header value."));
    	
    	
/* string manipulation */
    	names.put("cat", new Name("cat", toTableConsumer(s-> {
    		String sep = ((Column.OfConstantStrings)s.removeFirst()).getValue();
    		List<String> l = new ArrayList<>();
    		while(!s.isEmpty()) {
    			Column<?,?> c = s.removeFirst();
    			if(c instanceof ConstantColumn) {
    				l.add(((ConstantColumn<?,?>)c).getValue().toString());
    			} else {
    				throw new IllegalArgumentException("cat can only operate on constant columns."); 
    			}
    		}
   			s.addFirst(new Column.OfConstantStrings(l.stream().collect(Collectors.joining(sep))));
    	}),"[sep=\"\",:]", "Concatenates values of constant string columns."));
    	
/* array creation */
    	names.put("rolling", new Name("rolling", toTableConsumer(s-> {
    		int size = (int) ((Column.OfConstantDoubles) s.removeFirst()).getValue().doubleValue();
            Optional<Periodicity<Period>> windowFreq = s.peekFirst() instanceof Column.OfConstantStrings &&
            		((Column.OfConstantStrings)s.removeFirst()).getValue().equals("range") ? Optional.empty() :
            			Optional.of((Periodicity<Period>)((Column.OfConstantPeriodicities)s.removeFirst()).getValue());
    		s.replaceAll(c -> {
    			return new Column.OfDoubleArray1Ds(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " rolling",
    				inputs -> range -> {
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
    	}),"[c=2,periodicity=\"range\",:]", "Creates double array columns for each input column with rows containing values "+
    			"from rolling window of past data where the window is *c* periods of periodicity *periodicity*, defaulting to the evaluation periodicity."));
    	names.put("cross", new Name("cross", toTableConsumer(s-> {
    		Column.OfDoubles[] a = s.toArray(new Column.OfDoubles[]{});
    		s.clear();
    		s.add(new Column.OfDoubleArray1Ds(inputs -> "cross", inputs -> "cross", inputs -> range -> {
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
    	names.put("rev_expanding", new Name("rev_expanding", toTableConsumer(s-> {
    		s.replaceAll(c -> {
    			return new Column.OfDoubleArray1Ds(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " rev_expanding",
    				inputs -> range -> {
						Stream.Builder<DoubleStream> b = Stream.builder();
						double[] data = ((Column.OfDoubles)inputs[0]).rows(range).toArray();
						for(int i = 0; i < range.size(); i++) {
							b.accept(Arrays.stream(data, i, data.length));
						}
						return b.build();
					},c);
    		});
    		
    	}),"[:]", "Creates double array columns for each input column with rows " +
    		"containing values from the current period to the end of the range."));
    	names.put("expanding", new Name("expanding", toTableConsumer(s-> {
    		Column<?,?> col = s.removeFirst();
    		Optional<LocalDate> start = col instanceof Column.OfConstantDates ? Optional.of(((Column.OfConstantDates)col).getValue()) : Optional.empty();
    		col = s.removeFirst();
    		Optional<Periodicity<?>> periodicity = col instanceof Column.OfConstantPeriodicities ? Optional.of(((Column.OfConstantPeriodicities) col).getValue()) : Optional.empty();
    		boolean initialZero = Boolean.parseBoolean(((Column.OfConstantStrings)s.removeFirst()).getValue());
    		boolean nonZero = Boolean.parseBoolean(((Column.OfConstantStrings)s.removeFirst()).getValue());
    		s.replaceAll(c -> {
    			return new Column.OfDoubleArray1Ds(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " expanding",
    				inputs -> range -> {
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
								int startIndex = 0;
								if(nonZero) {
									startIndex = index+1;
									while(startIndex > 0 && data[startIndex-1] != 0) {
										startIndex--;
									}
								}
								b.accept(Arrays.stream(data, startIndex, index + 1));
							} else {
								b.accept(DoubleStream.iterate(Double.NaN, r -> Double.NaN).limit(range.size() + 1));
							}
						}
						return b.build();
					},c);
    		});
    	}),"[date=\"range\",periodicity=\"range\",initial_zero=\"false\",non_zero=\"false\",:]", "Creates double array columns for each input column with rows " +
    		"containing values from an expanding window of past data with periodicity *freq* that starts on date *start*."));


/* array operators */
    	for(DoubleCollectors dc : DoubleCollectors.values()) {
    		names.put(dc.name(), new Name(dc.name(), makeOperator(dc.name(), dc),"[:]","Aggregates row values in double array " +
    				"columns to a double value by " + dc.name()));
    	}


/* binary operators */
    	names.put("+", makeOperator("+", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x + y));
    	names.put("-", makeOperator("-", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x - y));
    	names.put("*", makeOperator("*", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x * y));
    	names.put("/", makeOperator("/", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x / y));
    	names.put("%", makeOperator("%", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x % y));
    	names.put("^", makeOperator("^", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : Math.pow(x, y)));
    	names.put("==", makeOperator("==", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x == y ? 1.0 : 0.0));
    	names.put("!=", makeOperator("!=", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN :  x != y ? 1.0 : 0.0));
    	names.put(">", makeOperator(">", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x > y ? 1.0 : 0.0));
    	names.put("<", makeOperator("<", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x < y ? 1.0 : 0.0));
    	names.put(">=", makeOperator(">=", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x >= y ? 1.0 : 0.0));
    	names.put("<=", makeOperator("<=", (x,y) -> Double.isNaN(x) || Double.isNaN(y) ? Double.NaN : x <= y ? 1.0 : 0.0));


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
    	names.put("zeroToNa", makeOperator("zeroToNa", x -> x == 0 ? Double.NaN : x));
    	names.put("xmPrice", makeOperator("xmPrice", quote -> {
    		double TERM = 10, RATE = 6, price = 0; 
    		for (int i = 0; i < TERM * 2; i++) {
    			price += RATE / 2 / Math.pow(1 + (100 - quote) / 2 / 100, i + 1);
    		}
    		return price + 100 / Math.pow(1 + (100 - quote) / 2 / 100, TERM * 2);
    	}));
    	names.put("ymPrice", makeOperator("ymPrice", quote -> {
    		double TERM = 3, RATE = 6, price = 0; 
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
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(4);
			nf.setGroupingUsed(false);
			String rowHeader = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			boolean colHeaders = Boolean.parseBoolean(((Column.OfConstantStrings) s.removeFirst()).getValue());
			Table t = new Table();
			t.insertAtTop(s);
			t.evaluate(range);
			String[] lines = t.toCsv(nf).split("\n");
			if(!rowHeader.equals("date")) {
				for(int i = 0; i < lines.length; i++) {
					String[] cols = lines[i].split(",");
					cols[0] = i == 0 ? "" : rowHeader;
					lines[i] = Arrays.stream(cols).collect(Collectors.joining(","));
				}
			}
			if(!colHeaders) {
				lines[0] = lines[0].replaceAll("[^,]", "&nbsp;");
			}
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
		}), "[periodicity=B, date=-20 offset today,format=\"decimal\",row_header=\"date\",col_headers=\"true\",:]",
				"Creates a const string column with code for an HTML ranking table.", false));
		names.put("chart", new Name("chart", p -> toTableConsumer(s -> {
			Periodicity<?> periodicity = ((Column.OfConstantPeriodicities) s.removeFirst()).getValue();
			LinkedList<LocalDate> d = new LinkedList<>();
			while ((!s.isEmpty()) && d.size() < 2 && s.peekFirst().getHeader().equals("date")) {
				d.add(((Column.OfConstantDates) s.removeFirst()).getValue());
			}
			PeriodicRange<?> range = periodicity.range(d.removeLast(), d.isEmpty() ? LocalDate.now() : d.peek(), false);
			String title = ((Column.OfConstantStrings) s.removeFirst()).getValue();
			String options = ((Column.OfConstantStrings) s.removeFirst()).getValue();
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
			String html = chartHTML.get().format(new Object[] { getId(), dates, data, title, options}, new StringBuffer(), null)
					.toString();
			s.clear();
			s.add(new Column.OfConstantStrings(html, "HTML"));
		}), "[periodicity=B, date=-20 offset today,title=\"\",options=\"\",:]",
				"Creates a const string column with code for an HTML chart.", false));
		names.put("rt", new Name("rt", p -> toTableConsumer(s -> {
			LinkedList<String> functions = new LinkedList<>();
			while ((!s.isEmpty()) && s.peekFirst().getHeader().equals("functions")) {
				functions.addFirst(((Column.OfConstantStrings) s.removeFirst()).getValue());
			}
			NumberFormat nf = ((Column.OfConstantStrings) s.removeFirst()).getValue().equals("percent")
					? NumberFormat.getPercentInstance() : NumberFormat.getNumberInstance();
			int digits = ((Column.OfConstantDoubles) s.removeFirst()).getValue().intValue();
			nf.setMaximumFractionDigits(digits);
			int columns = functions.size();
			int rows = s.size();
			String[] labels = new String[rows];
			String[] headers = new String[columns];
			String[][] cells = new String[rows][columns];
			for (int i = 0; i < columns; i++) {
				double[][] values = new double[s.size()][2];
				for (int j = 0; j < s.size(); j++) {
					Pinto.State state = new Pinto.State(false);
					final int J = j;
					state.setCurrent(t -> {
						LinkedList<Column<?,?>> stack = new LinkedList<>();
						stack.add(s.get(J).clone());
						t.insertAtTop(stack);
					});
					Table t = p.evaluate(functions.get(i), state).get(0);
					double[][] d = t.toColumnMajorArray().get();
					values[j][0] = d[0][d[0].length-1]; 
					values[j][1] = (int) j;
					if (j == 0) {
						headers[i] = t.getHeaders(false, false).get(0);
					}
					if(i == 0) {
						labels[j] =  s.get(j).getHeader();
					}
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
			s.clear();
			s.add(new Column.OfConstantStrings(sb.toString(), "HTML"));
		}), "[functions=\" BA-DEC offset expanding pct_change {YTD} today today eval\",format=\"percent\",digits=2,:]",
				"Creates a const string column with code for an HTML ranking table, applying each *function* to input columns and ranking the results.", false));
    	
    	
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
				if(right instanceof Column.OfConstantDoubles && lefts.get(i) instanceof Column.OfConstantDoubles) {
					stack.add(new Column.OfConstantDoubles(dbo.applyAsDouble(((Column.OfConstantDoubles)lefts.get(i)).getValue(),
							((Column.OfConstantDoubles)right).getValue())));
				} else if(right instanceof Column.OfDoubles && lefts.get(i) instanceof Column.OfDoubles) {
					stack.add(new Column.OfDoubles(inputs -> inputs[1].getHeader(), inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
						inputs -> range -> {
							OfDouble leftIterator = ((Column.OfDoubles)inputs[1]).rows(range).iterator();
							return  ((Column.OfDoubles)inputs[0]).rows(range).map(r -> dbo.applyAsDouble(leftIterator.nextDouble(), r));
						}, right, lefts.get(i)));
				} else if(right instanceof Column.OfDoubleArray1Ds && lefts.get(i) instanceof Column.OfDoubleArray1Ds) {
					stack.add(new Column.OfDoubleArray1Ds(inputs -> inputs[1].getHeader(), inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
						inputs -> range -> {
							Iterator<DoubleStream> leftIterator = ((Column.OfDoubleArray1Ds)inputs[1]).rows(range).iterator();
							return  ((Column.OfDoubleArray1Ds)inputs[0]).rows(range)
									.map(strm -> {
										OfDouble leftDoubleIterator = leftIterator.next().iterator();
										return strm.map(r -> dbo.applyAsDouble(leftDoubleIterator.nextDouble(), r));
									});
						}, right, lefts.get(i)));
				} else if(right instanceof Column.OfDoubles && lefts.get(i) instanceof Column.OfDoubleArray1Ds) {
					stack.add(new Column.OfDoubleArray1Ds(inputs -> inputs[1].getHeader(), inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
							inputs -> range -> {
								OfDouble leftIterator = ((Column.OfDoubles)inputs[0]).rows(range).iterator();
								return  ((Column.OfDoubleArray1Ds)inputs[1]).rows(range).map(rstrm -> {
									double l = leftIterator.nextDouble();
									return rstrm.map(r -> dbo.applyAsDouble(r, l));
								});
						}, right, lefts.get(i)));
				} else if(right instanceof Column.OfDoubleArray1Ds && lefts.get(i) instanceof Column.OfDoubles) {
					stack.add(new Column.OfDoubleArray1Ds(inputs -> inputs[1].getHeader(), inputs -> inputs[1].getTrace() + " " + inputs[0].getTrace() + " " + name,
						inputs -> range -> {
							OfDouble leftIterator = ((Column.OfDoubles)inputs[1]).rows(range).iterator();
							return  ((Column.OfDoubleArray1Ds)inputs[0]).rows(range).map(rstrm -> {
								double l = leftIterator.nextDouble();
								return rstrm.map(r -> dbo.applyAsDouble(l, r));
							});
						}, right, lefts.get(i)));
				} else {
					throw new IllegalArgumentException("Operator " + name + " can only operate on columns of doubles or double arrays.");
				}
			}
		}),"[width=1,:]", "Binary double operator " + name + " that operates on *width* columns at a time with fixed right-side operand.");
	}

	private static Name makeOperator(String name, DoubleUnaryOperator duo) {
		return new Name(name, toTableConsumer(stack -> {
			stack.replaceAll(c -> {
				if(c instanceof Column.OfDoubles) {
					return new Column.OfDoubles(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " " + name,
							inputs -> range -> ((Column.OfDoubles)inputs[0]).rows(range).map(duo), c);
				} else if(c instanceof Column.OfDoubleArray1Ds) {
					return new Column.OfDoubleArray1Ds(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " " + name,
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
				return new Column.OfDoubles(inputs -> inputs[0].getHeader(), inputs -> inputs[0].getTrace() + " " + name,
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
