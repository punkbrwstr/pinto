package tech.pinto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.StringTokenizer;

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

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Servlet.class);

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
		String path = request.getPathInfo();
		HttpSession session = request.getSession();
		if (session.getAttribute("pinto") == null) {
			session.setAttribute("pinto", pintoSupplier.get());
		}
		Pinto pinto = (Pinto) session.getAttribute("pinto");
		if(path == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if(path.contains("csv")) {
			getCSV(pinto, request, response);
		} else if(path.contains("console")) {
			getConsole(pinto, request, response);
		} else if(path.contains("complete")) {
			getCompletion(pinto, request, response);
		}
	}
	
	private void getCSV(Pinto pinto, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			response.setContentType("text/csv");
			response.setCharacterEncoding("UTF-8");
			if(!request.getParameterMap().containsKey("p")) {
				throw new Exception("Empty request.");
			}
			List<Table> l = pinto.execute(request.getParameter("p"));
			if(l.size() > 0) {
				response.getOutputStream().print(l.get(0).toCsv());
			}
		} catch (Exception e) {
			logError(e, request);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
		
	}

	private void getConsole(Pinto pinto, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			if(!request.getParameterMap().containsKey("p")) {
				throw new Exception("Empty request.");
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			List<Map<String,Object>> tables = new ArrayList<>();
			for(Table t : pinto.execute(request.getParameter("p"))) {
				ImmutableMap.Builder<String,Object> b = new ImmutableMap.Builder<String, Object>();
				b.put("output","<code>" + t.toString().replaceAll(" ", "&nbsp;") + "</code>");
				tables.add(b.build());
			}
			response.getOutputStream().print(gson.toJson(tables));
		} catch(Exception e) {
			sendJsonError(e, request, response);
		}
	}

	private void getCompletion(Pinto pinto, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			if(!request.getParameterMap().containsKey("p")) {
				throw new Exception("Empty request.");
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			List<CharSequence> candidates = new ArrayList<>();
			pinto.getNamespace().complete(request.getParameter("p"), 0, candidates);
			response.getOutputStream().print(gson.toJson(new ImmutableMap.Builder<String, Object>()
					.put("candidates", candidates).build()));
		} catch(Exception e) {
			sendJsonError(e, request, response);
		}
	}
	
	private void sendJsonError(Throwable t, HttpServletRequest request, HttpServletResponse response) throws IOException {
		logError(t,request);
		response.getOutputStream().print(gson.toJson(
				new ImmutableMap.Builder<String, Object>()
					.put("error", t.getMessage()).build()));
	}
	
	private void logError(Throwable t, HttpServletRequest request) {
		t.printStackTrace();
        if(t instanceof PintoSyntaxException || t instanceof IllegalArgumentException) {
            log.error("({}) syntax exception: {}",getClientIpAddress(request),t.getMessage());
        } else {
            log.error("({}) other exception",getClientIpAddress(request),t);
        }	
	}

    private static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return new StringTokenizer(xForwardedForHeader, ",").nextToken().trim();
        }
    }

}
