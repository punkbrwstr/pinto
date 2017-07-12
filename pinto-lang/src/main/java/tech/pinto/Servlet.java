package tech.pinto;

import java.io.IOException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import tech.pinto.function.TerminalFunction;
import tech.pinto.time.PeriodicRange;

import static java.util.stream.Collectors.toList;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Gson gson;
	private final Supplier<Pinto> pintoSupplier;

	public Servlet(Supplier<Pinto> pintoSupplier) {
		GsonBuilder b = new GsonBuilder();
		b.serializeSpecialFloatingPointValues();
		this.gson = b.create();
		this.pintoSupplier = pintoSupplier;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String statement = request.getParameter("statement");
		try {
			if (statement == null) {
				writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "error")
						.put("exception", "empty statement").build());
				return;
			}
			boolean numbersAsString = request.getParameterMap().containsKey("numbers_as_string");
			boolean omitDates = request.getParameterMap().containsKey("omit_dates");
			HttpSession session = request.getSession();
			if (session.getAttribute("pinto") == null) {
				session.setAttribute("pinto", pintoSupplier.get());
			}
			Pinto pinto = (Pinto) session.getAttribute("pinto");
			List<Map<String,Object>> responses = new ArrayList<>();
			for (TerminalFunction tf : pinto.execute(statement)) {
				ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
				builder.put("header",streamInReverse(tf.getColumnValues()).map(ColumnValues::getHeader)
							.map(h -> h.orElse("")).collect(Collectors.toList()));
				if (tf.getRange().isPresent()) {
					builder.put("responseType", "header_and_series");
					PeriodicRange<?> range = tf.getRange().get();
					builder.put("date_range",
							new ImmutableMap.Builder<String, String>().put("start", range.start().endDate().toString())
									.put("end", range.end().endDate().toString())
									.put("freq", range.periodicity().code()).build());
					if (!omitDates) {
						builder.put("index", range.dates().stream().map(LocalDate::toString).collect(toList()));
					}
					int columnCount = tf.getColumnValues().size();
					double[][] series = new double[columnCount][];
					double[] nullSeries = null;
					for (int i = columnCount - 1; i > -1; i--) {
						ColumnValues cv = tf.getColumnValues().get(i);
						if (cv.getSeries().isPresent()) {
							series[columnCount - i - 1] = cv.getSeries().get().toArray();
						} else {
							if (nullSeries == null) {
								nullSeries = new double[(int) tf.getRange().get().size()];
								Arrays.fill(nullSeries, Double.NaN);
								series[columnCount - i - 1] = nullSeries;
							}
						}
					}
					if (!numbersAsString) {
						builder.put("series", series);
					} else {
						builder.put("series", Stream.of(series).map(DoubleStream::of)
								.map(ds -> ds.mapToObj(Double::toString).collect(toList())).collect(toList()));
					}
				} else {
					builder.put("responseType", "header_only");
				}
				responses.add(builder.build());
			}
			writeResponse(response, responses);
		} catch (Throwable t) {
			t.printStackTrace();
			writeResponse(response, Arrays.asList(new ImmutableMap.Builder<String, Object>().put("responseType", "error")
					.put("exception", t).build()));
		}
	}

	protected void writeResponse(HttpServletResponse response, Object o) throws IOException {
		response.getOutputStream().print(gson.toJson(o));
	}

	 private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
	 Iterator<T> descendingIterator = input.descendingIterator();
	 return
	 StreamSupport.stream(Spliterators.spliteratorUnknownSize(descendingIterator,
	 Spliterator.ORDERED),
	 false);
	 }

}
