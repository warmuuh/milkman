package milkman.ui.plugin.rest.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.client.methods.RequestBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javafx.beans.property.StringProperty;
import lombok.Data;
import milkman.ui.plugin.Templater;

@Data
public class RestQueryParamAspect extends RestRequestAspect {

	private List<QueryParamEntry> entries = new LinkedList<>();
	
	public RestQueryParamAspect() {
		super("query");
	}
	
	@Override
	public void enrichRequest(RequestBuilder builder, Templater templater) throws Exception {
		/* nothing to do as we did this on-the-fly */
	}
	
	public void linkToUrlTextfield(StringProperty urlProperty) {
		urlProperty.addListener((obs, o, n) -> {
			if (o != n && n != null) {
				parseQueryParams(n);
				onInvalidate.invoke();
			}
		});
	}
	
	
	public void parseQueryParams(String url) {
		entries.clear();
		int indexOf = url.indexOf('?');
		if (indexOf > 0) {
			String paramString = url.substring(indexOf+1);
			String[] paramPairs = paramString.split("&");
			for(String pair : paramPairs) {
				String[] splittedPair = pair.split("=");
				String key = splittedPair.length > 0 ? splittedPair[0] : "";
				String value = splittedPair.length > 1 ? splittedPair[1] : "";
				entries.add(new QueryParamEntry(UUID.randomUUID().toString(), key, value));
			}
		}
	}
	
	public String generateNewUrl(String url) {
		int indexOf = url.indexOf('?');
		String baseUrl = url.split("\\?")[0];
		String qryString = entries.stream().map(e -> e.getName() + "=" + e.getValue()).collect(Collectors.joining("&"));
		return baseUrl + "?" + qryString;
	}
	
}
