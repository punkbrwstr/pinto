package tech.pinto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
			List<Pinto.Response> output = pinto.execute(statement);
			List<TimeSeries> data = output.stream().map(Pinto.Response::getTimeseriesOutput)
					.filter(Optional::isPresent).map(Optional::get).flatMap(List::stream)
					.collect(Collectors.toList());
			List<String> messages = output.stream().map(Pinto.Response::getMessageOutput)
					.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
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
						.put("columns", data.stream().map(TimeSeries::getLabel).collect(Collectors.toList()))
						.put("data",
								numbersAsString
										? data.stream().map(TimeSeries::stream)
												.map(ds -> ds.mapToObj(Double::toString)
														.collect(Collectors.toList()))
												.collect(Collectors.toList())
										: data.stream().map(TimeSeries::stream).map(DoubleStream::toArray)
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

}
