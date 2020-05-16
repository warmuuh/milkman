package milkman.ui.plugin.rest.postman.exporters;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.milkman.rest.postman.schema.v21.*;
import com.milkman.rest.postman.schema.v21.Body.Mode;
import lombok.SneakyThrows;
import milkman.domain.Collection;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

//TODO: needs to support folder-export as well
public class PostmanExporterV21 {

	private final static Pattern urlPattern = Pattern.compile("(?<protocol>.+?):\\/\\/(?<host>[^$\\/?:]+)(?::(?<port>[\\d]+))?(?<path>\\/[^$\\?]*)?(?:\\?(?<query>.*))?");

	@SneakyThrows
	public String export(Collection collection) {
		PostmanCollection210 pmCol = new PostmanCollection210();
		
		Info info = new Info();
		info.setName(collection.getName());
		info.setPostmanId(collection.getId());
		info.setSchema("https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
		pmCol.setInfo(info);
		pmCol.setItem(new LinkedList<ItemGroup>());
		collection.getRequests().stream()
			.filter(RestRequestContainer.class::isInstance)
			.map(RestRequestContainer.class::cast)
			.map(this::toItemGroup)
			.forEach(pmCol.getItem()::add);
		return writeJson(pmCol);
	}

	private <T> String writeJson(T value) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writeValueAsString(value);
	}

	
	private ItemGroup toItemGroup(RestRequestContainer request) {
		
		Request r = new Request();
		r.setMethod(Request.Method.fromValue(request.getHttpMethod()));
		
		List<HeaderEntry> headers = request.getAspect(RestHeaderAspect.class)
			.map(h -> h.getEntries()).orElse(Collections.emptyList());
		r.setHeader(new LinkedList<Header>());
		headers.forEach(h -> {
			Header newHeader = new Header();
			newHeader.setKey(h.getName());
			newHeader.setValue(h.getValue());
			r.getHeader().add(newHeader);
		});
		
		
		String bodyStr = request.getAspect(RestBodyAspect.class).map(b -> b.getBody()).orElse("");
		
		Body body = new Body();
		body.setMode(Mode.RAW);
		body.setRaw(bodyStr);
		r.setBody(body);


		var requestUrl = request.getUrl();
		if (StringUtils.isNotBlank(requestUrl)){
			Url url = parseUrl(requestUrl);
			r.setUrl(url);
		}


		ItemGroup itm = new ItemGroup();
		itm.setName(request.getName());
		itm.setRequest(r);
		return itm;
	}

	/* package */ Url parseUrl(String requestUrl) {
		Url url = new Url();
		url.setRaw(requestUrl);

		var matcher = urlPattern.matcher(requestUrl);
		if (matcher.find()){
			url.setHost(matcher.group("host"));
			url.setProtocol(matcher.group("protocol"));
			url.setPath(matcher.group("path"));
			if (matcher.group("query") != null){
				String[] qry = matcher.group("query").split("&");
				for(String q : qry) {
					String[] strings = q.split("=");
					if(strings.length == 2) {
						if (url.getQuery() == null)
							url.setQuery(new LinkedList<Query>());
						Query pmQuery = new Query();
						pmQuery.setKey(strings[0]);
						pmQuery.setValue(strings[1]);
						url.getQuery().add(pmQuery);
					} else if (strings.length == 1) {
						if (url.getQuery() == null)
							url.setQuery(new LinkedList<Query>());
						Query pmQuery = new Query();
						pmQuery.setKey(strings[0]);
						pmQuery.setValue("");
						url.getQuery().add(pmQuery);
					}
				}
			}
			url.setPort(matcher.group("port"));
		}
		return url;
	}
}
