package tech.pinto;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import tech.pinto.Pinto.Expression;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Servlet.class);

	private final Gson gson;
	private final Supplier<Pinto> pintoSupplier;
	Optional<MessageFormat> reportTemplate = Optional.empty();

	public Servlet(Supplier<Pinto> pintoSupplier) {
		GsonBuilder b = new GsonBuilder();
		b.serializeSpecialFloatingPointValues();
		this.gson = b.create();
		this.pintoSupplier = pintoSupplier;
	}
	
	private void getReport(Pinto pinto, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			ServletOutputStream os = response.getOutputStream();
			if(!request.getParameterMap().containsKey("p")) {
				throw new Exception("Empty request.");
			}
			if(!reportTemplate.isPresent()) {
				reportTemplate = Optional.of(new MessageFormat(readInputStreamIntoString(
						(getClass().getClassLoader().getResourceAsStream("report_template.html")))));
			}
			Table t = new Table();
			pinto.getNamespace().getName("~report-" + request.getParameter("p"))
					.getTableFunction().accept(pinto, t);
			StringBuilder sb = new StringBuilder();
			for(Column<?> c : t.flatten()) {
				sb.append(c.cast(String.class).rows(null));
			}
			os.print(reportTemplate.get().format(new Object[] {sb.toString()}, new StringBuffer(),null).toString());
		} catch (Exception e) {
			logError(e, request);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(10*60);
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
		} else if(path.contains("sas")) {
			getSas(pinto, request, response);
		} else if(path.contains("report")) {
			getReport(pinto, request, response);
		}
	}
	
	private void getCSV(Pinto pinto, HttpServletRequest request, HttpServletResponse response) throws IOException {
		getCSV(pinto,request,response,"NA", 10);
	}
	
	private void getSas(Pinto pinto, HttpServletRequest request, HttpServletResponse response) throws IOException {
		getCSV(pinto,request,response,".", 10);
	}

	private void getCSV(Pinto pinto, HttpServletRequest request, HttpServletResponse response, String naLiteral, int digits ) throws IOException {
		try {
			response.setContentType("text/csv");
			response.setCharacterEncoding("UTF-8");
			if(!request.getParameterMap().containsKey("p")) {
				throw new Exception("Empty request.");
			}
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setNaN(naLiteral);
			dfs.setInfinity(naLiteral);
			DecimalFormat nf = new DecimalFormat();
			nf.setDecimalFormatSymbols(dfs);
			nf.setGroupingUsed(false);
            nf.setMinimumFractionDigits(1);
            nf.setMaximumFractionDigits(8);
			//List<Table> l = pinto.evaluate(request.getParameter("p"));
			Table t = null;
			var lines = request.getParameter("p").split("\n");
			var e = new Pinto.Expression(false);
			for(var line : lines) {
				var expressions =  pinto.parse(line, e);
				var next = expressions.size() > 0 
						&& !expressions.get(expressions.size()-1).hasTerminal() ?
								expressions.remove(expressions.size()-1) : new Expression(false);
				for(int i = 0; i < expressions.size(); i++) {
					t = expressions.get(i).evaluate(pinto);
				}
				e = next;
			}
			if(!t.getStatus().isPresent()) {
				response.getOutputStream().print(t.toCsv(nf));
			} else {
				response.getOutputStream().print("Output\n" + t.getStatus().orElse("") + "\n");
			}
//			if(l.size() > 0) {
//				if(!l.get(l.size() - 1).getStatus().isPresent()) {
//					response.getOutputStream().print(l.get(l.size()-1).toCsv(nf));
//				} else {
//					response.getOutputStream().print("Output\n" + l.get(l.size()-1).getStatus().orElse("") + "\n");
//				}
//			}
		} catch (Exception e) {
			logError(e, request);
			response.getOutputStream().print("Error\n" + e.getMessage().replaceAll(",", ";") + "\n");
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
			for(Table t : pinto.evaluate(request.getParameter("p"))) {
				ImmutableMap.Builder<String,Object> b = new ImmutableMap.Builder<String, Object>();
				if(!t.getStatus().isPresent()) {
					b.put("output","<code>" + t.getConsoleText(false).replaceAll(" ", "&nbsp;") + "</code>");
				} else {
					b.put("output","<code>" + t.getStatus().orElse("").replaceAll(" ", "&nbsp;") + "</code>");
				}
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
	
	private String readInputStreamIntoString(InputStream inputStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while(result != -1) {
		    buf.write((byte) result);
		    result = bis.read();
		}
		// StandardCharsets.UTF_8.name() > JDK 7
		return buf.toString("UTF-8");
	}
}
