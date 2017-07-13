package tech.pinto;

import java.io.IOException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
			for (Table t : pinto.execute(statement)) {
				ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
				builder.put("header",t.getHeaders());
				if (t.getRange().isPresent()) {
					builder.put("responseType", "header_and_series");
					builder.put("date_range", t.getRange().get().asStringMap());
					if (!omitDates) {
						builder.put("index", t.getRange().get().dates().stream().map(LocalDate::toString).collect(toList()));
					}
					builder.put("series", !numbersAsString ? t.toColumnMajorArray().get() :
							Stream.of(t.toColumnMajorArray().get()).map(DoubleStream::of)
								.map(ds -> ds.mapToObj(Double::toString).collect(toList())).collect(toList()));
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


}
