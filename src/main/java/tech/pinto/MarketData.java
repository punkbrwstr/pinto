package tech.pinto;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import tech.pinto.Pinto.StackFunction;
import tech.pinto.time.Period;
import tech.pinto.time.PeriodicRange;

public interface MarketData {

	public <P extends Period<P>> Function<PeriodicRange<?>, double[][]> getRowFunction(Request request);
	public String getDefaultField();

	default public StackFunction getStackFunction(String literal) {
		return  (p,s) -> {
		    var req = fromLiteral(literal);
			for(int i = 0; i < req.size(); i++) {
				s.addFirst(getColumn(req, i));
			}
			Cache.putFunction(req.getSecurityFieldsString(), req.size(), getRowFunction(req));
		};
	}
	
	default public Column<double[]> getColumn(Request request, int col) {
		return new Column<double[]>(double[].class,i -> request.getSecurityFields().get(col),i -> "$" + request.getSecurityFields().get(col) +"$",
			(range, inputs) -> {
				return Cache.getCachedRows(request.getSecurityFieldsString(), col, range);
			});
	}

	default public List<Column<String>> getStaticData(String literal) {
	    return getStaticData(fromLiteral(literal));
    }

	default public List<Column<String>> getStaticData(Request request) {
		throw new UnsupportedOperationException();
	}


	default public Request fromLiteral(String literal) {
		String[] securitiesAndFields = literal.split(":");
		String fieldsString = securitiesAndFields.length > 1 ? securitiesAndFields[1] : getDefaultField();
		return new Request(Arrays.asList(securitiesAndFields[0].split(",")),
				Arrays.asList(fieldsString.split(",")));
	}

	public static class Request {
		private final List<String> securities;
		private final List<String> fields;
		private final List<String> securityFields = new ArrayList<>();
		private final HashMap<String,Integer> securityFieldOrdinals = new HashMap<>();
		private final String securityFieldsString;
		private final Map<String,String> overrides;


		public Request(List<String> securities, List<String> fields) {
		    this(securities, fields, new HashMap<>());
        }

		public Request(List<String> securities, List<String> fields, Map<String,String> overrides) {
			super();
			this.securities = securities.stream().map(s -> {
				String[] securityArray = s.trim().split(" ");
				for(int j = 0; j < securityArray.length - 1; j++) {
					securityArray[j] = securityArray[j].toUpperCase();
				}
				securityArray[securityArray.length-1] = securityArray[securityArray.length-1].substring(0, 1).toUpperCase()
						.concat(securityArray[securityArray.length-1].toLowerCase().substring(1));
				return String.join(" ", securityArray);

			}).collect(Collectors.toList());
			this.fields = fields.stream().map(f -> f.trim()).map(String::toUpperCase)
					.map(f -> f.replaceAll(" ", "_")).collect(Collectors.toList());
			this.overrides = overrides;
			for(int i = 0; i < securities.size(); i++) {
				for(int j = 0; j < fields.size(); j++) {
					String securityField = this.securities.get(i).concat(":").concat(this.fields.get(j));
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

		public Map<String,String> getOverrides() {
			return overrides;
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
