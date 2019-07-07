package milkman.ui.plugin.rest.postman.exporters;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.milkman.rest.postman.schema.v21.Body;
import com.milkman.rest.postman.schema.v21.Header;
import com.milkman.rest.postman.schema.v21.Info;
import com.milkman.rest.postman.schema.v21.ItemGroup;
import com.milkman.rest.postman.schema.v21.PostmanCollection210;
import com.milkman.rest.postman.schema.v21.Query;
import com.milkman.rest.postman.schema.v21.Request;
import com.milkman.rest.postman.schema.v21.Url;

import io.vavr.control.Try;
import lombok.SneakyThrows;

import com.milkman.rest.postman.schema.v21.Body.Mode;

import milkman.domain.Collection;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

//TODO: needs to support folder-export as well
public class PostmanExporterV21 {

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
		
		
		Url url = new Url();
		url.setRaw(request.getUrl());
		
		Try<URI> tUri = Try.of(() -> new URI(request.getUrl()));
		
		url.setHost(tUri.map(u -> u.getHost().split("\\.")).getOrElse(new String[] {}));

		url.setProtocol(tUri.map(u -> u.getScheme()).getOrNull());
		url.setPath(tUri.map(u -> u.getPath().split("/")).getOrElse(new String[] {}));
		
		String[] qry = tUri.map(u -> u.getQuery().split("&")).getOrElse(new String[] {});
		for(String q : qry) {
			String[] strings = q.split("=");
			if(strings.length == 2) {
				if (url.getQuery() == null)
					url.setQuery(new LinkedList<Query>());
				Query pmQuery = new Query();
				pmQuery.setKey(strings[0]);
				pmQuery.setValue(strings[1]);
				url.getQuery().add(pmQuery);
			}
		}
		
		url.setPort(tUri.map(u -> u.getPort()).filter(p -> p > 0).map(Object::toString).getOrNull());
		
		r.setUrl(url);
		
		ItemGroup itm = new ItemGroup();
		itm.setName(request.getName());
		itm.setRequest(r);
		return itm;
	}
}
