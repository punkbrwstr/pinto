package tech.pinto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			if(session.getAttribute("pinto") == null) {
				session.setAttribute("pinto", pintoSupplier.get());
			}
			Pinto pinto = (Pinto) session.getAttribute("pinto");
			LinkedList<TimeSeries> data = new LinkedList<>();
			List<String> messages = new ArrayList<>();
	    	for(TerminalFunction tf : pinto.execute(statement)) {
	    		Optional<LinkedList<TimeSeries>> list = tf.getTimeSeries();
	    		list.ifPresent(data::addAll); 
	    		tf.getText().ifPresent(messages::add);
	    	}
			if (data.size() == 0 && messages.size() == 0) {
				writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "none").build());
			} else if (data.size() > 0) {
				writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "data")
						.put("date_range",
								new ImmutableMap.Builder<String, String>()
										.put("start", data.get(0).getRange().start().endDate().toString())
										.put("end", data.get(0).getRange().end().endDate().toString())
										.put("freq", data.get(0).getRange().periodicity().code()).build())
						.put("index",
								omitDates ? ""
										: data.get(0).getRange().dates().stream().map(LocalDate::toString)
												.collect(Collectors.toList()))
						.put("columns", streamInReverse(data).map(TimeSeries::getLabel).collect(Collectors.toList()))
						//.put("columns", data.stream().map(TimeSeries::getLabel).collect(Collectors.toList()))
						.put("data", numbersAsString ? streamInReverse(data).map(TimeSeries::stream)
												.map(ds -> ds.mapToObj(Double::toString).collect(Collectors.toList()))
												.collect(Collectors.toList())
										: streamInReverse(data).map(TimeSeries::stream).map(DoubleStream::toArray)
												.collect(Collectors.toList()))
						.build());
			} else {
				writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "messages")
						.put("messages", messages).build());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			writeResponse(response, new ImmutableMap.Builder<String, Object>().put("responseType", "error").put("exception", t)
					.build());
		}
	}
	
	protected void writeResponse(HttpServletResponse response, Object o) throws IOException {
		response.getOutputStream().print(gson.toJson(o));
	}
	
	private static <T> Stream<T> streamInReverse(LinkedList<T> input) {
		  Iterator<T> descendingIterator = input.descendingIterator();
		  return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
		    descendingIterator, Spliterator.ORDERED), false);
	}

}
