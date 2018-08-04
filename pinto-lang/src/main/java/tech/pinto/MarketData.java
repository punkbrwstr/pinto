package tech.pinto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import tech.pinto.Pinto.StackFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public interface MarketData {

	public <P extends Period<P>> Function<PeriodicRange<?>, double[][]> getFunction(Request request);
	public String getDefaultField();
					
	default public StackFunction getStackFunction(String literal) {
		return  (p,s) -> {
			String[] sa = literal.split(":");
			String[] securitiesArray = sa[0].split(",");
			for(int i = 0; i < securitiesArray.length; i++) {
				String[] securityArray = securitiesArray[i].trim().split(" ");
				for(int j = 0; j < securityArray.length - 1; j++) {
					securityArray[j] = securityArray[j].toUpperCase();
				}
				securityArray[securityArray.length-1] = securityArray[securityArray.length-1].substring(0, 1).toUpperCase()
						.concat(securityArray[securityArray.length-1].toLowerCase().substring(1));
				securitiesArray[i] = String.join(" ", securityArray);
			}
			List<String> securities = Arrays.asList(securitiesArray);
			String fieldsString = sa.length > 1 ? sa[1] : getDefaultField();
			List<String> fields = Arrays.stream(fieldsString.split(",")).map(f -> f.trim()).map(String::toUpperCase)
					.map(f -> f.replaceAll(" ", "_")).collect(Collectors.toList());	
			Request req = new Request(securities, fields);
			for(int i = 0; i < req.size(); i++) {
				s.addFirst(getColumn(req, i));
			}
			Cache.putFunction(req.getSecurityFieldsString(), req.size(), getFunction(req));
		};
	}
	
	default public Column.OfDoubles getColumn(Request request, int col) {
		return new Column.OfDoubles(i -> request.getSecurityFields().get(col),
			(range, inputs) -> {
				return Cache.getCachedRows(request.getSecurityFieldsString(), col, range);
			});
	}
	
	public static class Request {
		private final List<String> securities;
		private final List<String> fields;
		private final List<String> securityFields = new ArrayList<>();
		private final HashMap<String,Integer> securityFieldOrdinals = new HashMap<>();
		private final String securityFieldsString;
		public Request(List<String> securities, List<String> fields) {
			super();
			this.securities = securities;
			this.fields = fields;
			for(int i = 0; i < securities.size(); i++) {
				for(int j = 0; j < fields.size(); j++) {
					String securityField = securities.get(i).concat(":").concat(fields.get(j));
					securityFields.add(securityField);
					securityFieldOrdinals.put(securityField, i * fields.size() + j);
				}
			}
			this.securityFieldsString = String.join(",", securityFields);
		}
		public int size() {
			return securityFields.size();
		}
		public int getOrdinal(String securityField) {
			return securityFieldOrdinals.get(securityField);
		}
		public List<String> getSecurities() {
			return securities;
		}
		public List<String> getFields() {
			return fields;
		}
		public List<String> getSecurityFields() {
			return securityFields;
		}
		public String getSecurityFieldsString() {
			return securityFieldsString;
		}

		@Override
		public int hashCode() {
			return Objects.hash(securityFieldsString);
		}

		@Override
		public boolean equals(Object obj) {
			return Objects.equals(this, obj);
		}
		
	}
	

}
