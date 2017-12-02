package tech.pinto.extras;

import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;

import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public class BloombergClient {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	private static final int TIMEOUT = 5000;

	private final AtomicLong requestNumber = new AtomicLong();
	private Session session;
	private final ConcurrentMap<Long, Job> jobs = new ConcurrentHashMap<>();
	private final SessionOptions options;

	public BloombergClient() {
		options = new SessionOptions();
		options.setServerHost("localhost");
		options.setServerPort(8194);
	}

	private void connect() {
		try {
			session = new Session(options, (event, session) -> {
				MessageIterator msgIter = event.messageIterator();
				while (msgIter.hasNext()) {
					Message msg = msgIter.next();
					if (msg.messageType().toString().equals("SessionConnectionUp")) {
						log.info("Bloomberg connection up.");
						continue;
					}
					if (msg.messageType().toString().equals("SessionStarted")) {
						log.info("Bloomberg session started.");
						continue;
					}
					Job j = jobs.get(msg.correlationID().value());
					try {
						if (!(event.eventType().equals(Event.EventType.RESPONSE)
								|| event.eventType().equals(Event.EventType.PARTIAL_RESPONSE))) {
							return;
						}
						if (msg.hasElement("responseError")) {
							throw new Exception("Response error: "
									+ msg.getElement("responseError").getElement("message").getValueAsString());
						}
						Element securityDataRaw = msg.getElement("securityData"); // maybe one maybe many
						List<Element> securityElements = new ArrayList<Element>();
						Consumer<Element> processSecurityElement = e -> {
							if (e.hasElement("securityError")) {
								j.getErrorHandler().accept(new IllegalArgumentException("\""
										+ e.getElementAsString("security") + ": \""
										+ e.getElement("securityError").getElement("message").getValueAsString()));
							} else if (e.hasElement("fieldExceptions")
									&& e.getElement("fieldExceptions").numValues() != 0) {
								Element exceptions = e.getElement("fieldExceptions");
								Element field = exceptions.getValueAsElement(0).getElement("fieldId");
								Element reason = exceptions.getValueAsElement(0).getElement("errorInfo")
										.getElement("message");
								j.getErrorHandler().accept(new IllegalArgumentException(
										"\"" + field.getValueAsString() + "\": " + reason.getValueAsString()));
							}
							securityElements.add(e);
						};
						if (securityDataRaw.isArray()) {
							for (int i = 0; i < securityDataRaw.numValues(); i++) {
								processSecurityElement.accept(securityDataRaw.getValueAsElement(i));
							}
						} else {
							processSecurityElement.accept(securityDataRaw);
						}
						j.addElements(securityElements);
						if (event.eventType().equals(Event.EventType.RESPONSE)) {
							j.finish();
						}
					} catch (Exception e) {
						j.getErrorHandler().accept(e);
					}
				}
			});
			if (!session.start())
				throw new Exception("Failed to start Bloomberg session.");
			if (!session.openService("//blp/refdata"))
				throw new Exception("Failed to open //blp/refdata");
		} catch (Exception e) {
			throw new RuntimeException("Unable to open bloomberg session.", e);
		}
	}

	public <P extends Period> Function<PeriodicRange<?>, List<DoubleStream>> 
					getFunction(List<String> securities, List<String> fields ) {
		return (range) -> {
			if (session == null) {
				connect();
			}
			final long jobNumber = requestNumber.incrementAndGet();
			final List<String> securityCodes = securities;
			final List<String> fieldCodes = fields;
			final List<String> securityCodeFieldCode = securityCodes.stream()
						.flatMap(s -> fieldCodes.stream().map(c -> s + ":" + c)).collect(Collectors.toList());
			final Map<String,Integer> rowMap = IntStream.range(0,securityCodeFieldCode.size()).mapToObj(Integer::valueOf)
						.collect(Collectors.toMap((i) -> securityCodeFieldCode.get(i), Function.identity()));
			final double[][] data = new double[securityCodeFieldCode.size()][(int) range.size()];
			Arrays.stream(data).forEach(d -> Arrays.fill(d, Double.NaN));
			final CompletableFuture<List<DoubleStream>> futureDS = new CompletableFuture<>();
//			final Map<String,DoubleStream.Builder> builders = securityCodeFieldCode.stream()
//						.collect(Collectors.toMap(Function.identity(), (s) -> DoubleStream.builder()));
//			final Map<String,AtomicInteger> stillNeeded = securityCodeFieldCode.stream()
//						.collect(Collectors.toMap(Function.identity(), (s) -> new AtomicInteger((int) d.size())));
			final Consumer<Exception> errorHandler = e -> {
				futureDS.completeExceptionally(e);
			};
			try {
				synchronized (session) {
					Service refDataService = session.getService("//blp/refdata");
					com.bloomberglp.blpapi.Request request = refDataService.createRequest("HistoricalDataRequest");
					log.info("history: {} {}",securityCodeFieldCode.stream().collect(Collectors.joining(",")),range.toString());
					securityCodes.stream().forEach(request.getElement("securities")::appendValue);
					fieldCodes.stream().forEach(request.getElement("fields")::appendValue);
					request.set("periodicitySelection", range.periodicity().bloombergCode());
					request.set("startDate", range.start().endDate().format(DateTimeFormatter.BASIC_ISO_DATE));
					LocalDate endDate = Arrays.asList("BM", "BQ-DEC", "BA-DEC").contains(range.periodicity().code())
							? range.end().endDate().withDayOfMonth(range.end().endDate().lengthOfMonth()) : range.end().endDate();

					request.set("endDate", endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
					request.set("maxDataPoints", 10000);
					request.set("returnEids", true);
					request.set("nonTradingDayFillOption", "NON_TRADING_WEEKDAYS");
					request.set("nonTradingDayFillMethod", "NIL_VALUE");
					if(Arrays.asList("W-MON", "W-TUE", "W-WED", "W-THU").contains(range.periodicity().code())) {
						request.set("periodicityAdjustment", "ACTUAL");
					}
					//request.set("nonTradingDayFillMethod", fillPrevious ? "PREVIOUS_VALUE" : "NIL_VALUE");
					jobs.put(jobNumber, new Job(l -> {
						for (Element securityData : l) {
							Element fieldElement = securityData.getElement("fieldData");
							for (int i = 0; i < fieldElement.numValues(); ++i) {
								Element dateValueElement = fieldElement.getValueAsElement(i);
								Datetime date = dateValueElement.getElement(0).getValueAsDate();
								if (range.periodicity().code().equals("B")
										&& (date.calendar().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
												|| date.calendar().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
									continue;
								}
								if (dateValueElement.numElements() == 1) {
									//TODO
									//builders.get(securityField).add(Double.NaN);
								} else {
									for(int j = 1; j < dateValueElement.numElements(); j++) {
										int row = rowMap.get(securityData.getElement("security").getValueAsString() 
												+ ":" + dateValueElement.getElement(j).name());
										int col = (int) range.indexOf(LocalDate.parse(date.toString()));
										data[row][col] = dateValueElement.getElement(j).getValueAsFloat64();
//										builders.get(securityField).add(dateValueElement.getElement(1).getValueAsFloat64());
//										stillNeeded.get(securityField).decrementAndGet();
									}
								}
							}
						}
						jobs.remove(jobNumber);
//						for(String securityField : securityCodeFieldCode) {
//							for (int i = 0; i < stillNeeded.get(securityField).get(); i++) {
//								builders.get(securityField).add(Double.NaN);
//							}
//						}
						futureDS.complete(IntStream.range(0, securityCodeFieldCode.size())
								.mapToObj(i -> DoubleStream.of(data[i])).collect(Collectors.toList()));
//						futureDS.complete(builders.entrySet().stream()
//								.map(e -> new DoubleData(d, e.getKey(), e.getValue().build()))
//								.collect(Collectors.toCollection(() -> new ArrayDeque<DoubleData>())));
					}, errorHandler));
					session.sendRequest(request, new CorrelationID(jobNumber));
				}
				return futureDS.get(TIMEOUT, TimeUnit.SECONDS);
			} catch (Exception e) {
				String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
				session = null;
				throw new RuntimeException("Bloomberg error: " + message, e);
			}
		};
	}

	public String getStaticData(String code, String field) {
		return getStaticData(new StaticDataRequest(code, field)).get(code).get(field);
	}

	public Map<String, Map<String, String>> getStaticData(StaticDataRequest sdr) {
		if (session == null) {
			connect();
		}
		final long jobNumber = requestNumber.incrementAndGet();
		final CompletableFuture<Map<String, Map<String, String>>> futureMap = new CompletableFuture<>();
		final Map<String, Map<String, String>> data = new HashMap<>();
		final Consumer<Exception> errorHandler = e -> {
			futureMap.completeExceptionally(e);
		};
		try {
			synchronized (session) {
				Service refDataService = session.getService("//blp/refdata");
				com.bloomberglp.blpapi.Request request = refDataService.createRequest("ReferenceDataRequest");
				sdr.getSecurities().stream().forEach(s -> request.getElement("securities").appendValue(s));
				sdr.getFields().stream().forEach(f -> request.getElement("fields").appendValue(f));
				sdr.getOverrides().entrySet().stream().forEach(e -> {
					Element override = request.getElement("overrides").appendElement();
					override.setElement("fieldId", e.getKey());
					override.setElement("value", e.getValue());
				});
				request.set("returnEids", true);
				jobs.put(jobNumber, new Job(l -> {
					for (Element securityData : l) {
						String security = securityData.getElement("security").getValueAsString();
						if (!data.containsKey(security)) {
							data.put(security, new HashMap<>());
						}
						Element fieldElements = securityData.getElement("fieldData");
						for (int i = 0; i < fieldElements.numElements(); ++i) {
							Element fieldElement = fieldElements.getElement(i);
							String field = fieldElement.elementDefinition().name().toString();
							data.get(security).put(field, fieldElement.getValueAsString());
						}
					}
					jobs.remove(jobNumber);
					futureMap.complete(data);
				}, errorHandler));
				session.sendRequest(request, new CorrelationID(jobNumber));
			}
			return futureMap.get(1000, TimeUnit.SECONDS);
		} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException("Unable to complete Bloomberg static data request: " + sdr.toString(), e);
		}
	}

	private static class Job {
		private final Consumer<List<Element>> responseHandler;
		private final Consumer<Exception> errorHandler;
		private final List<Element> elements = new ArrayList<>();

		Job(Consumer<List<Element>> responseHandler, Consumer<Exception> errorHandler) {
			this.responseHandler = responseHandler;
			this.errorHandler = errorHandler;
		}

		void addElements(List<Element> l) {
			elements.addAll(l);
		}

		void finish() {
			responseHandler.accept(elements);
		}

		Consumer<Exception> getErrorHandler() {
			return errorHandler;
		}
	}

	public static class StaticDataRequest {

		private List<String> securities;
		private List<String> fields;
		private Map<String, String> overrides;

		public StaticDataRequest(String code, String field) {
			this(Arrays.asList(code), Arrays.asList(field), new HashMap<>());
		}

		public StaticDataRequest(List<String> securities, List<String> fields, Map<String, String> overrides) {
			this.securities = securities;
			this.fields = fields;
			this.overrides = overrides;
		}

		public List<String> getSecurities() {
			return securities;
		}

		public List<String> getFields() {
			return fields;
		}

		public Map<String, String> getOverrides() {
			return overrides;
		}

		public String toString() {
			return securities.stream().collect(Collectors.joining(",")) + ":"
					+ fields.stream().collect(Collectors.joining(",")) + ":" + overrides.entrySet().stream()
							.map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(","));
		}

	}

}
