package milkman.ui.plugin.rest.postman;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.poynt.postman.model.PostmanCollection;
import co.poynt.postman.model.PostmanContainer;
import co.poynt.postman.model.PostmanEnvValue;
import co.poynt.postman.model.PostmanEnvironment;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class PostmanImporter {

	public Collection importCollection(String json) throws Exception {
		PostmanCollection pmCollection = readJson(json, PostmanCollection.class);
		return convertToDomain(pmCollection);
	}

	private <T> T readJson(String json, Class<T> type) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		T pmCollection = mapper.readValue(json, type);
		return pmCollection;
	}

	private Collection convertToDomain(PostmanCollection pmCollection) {
		//flattening folders:
		List<RequestContainer> requests = new LinkedList<>();
		for(PostmanContainer container : pmCollection.item) {
			requests.addAll(convertToDomain(container));	
		}

		return new Collection(pmCollection.info.name, requests);
	}
	private List<RequestContainer> convertToDomain(PostmanContainer pmItem) {
		List<RequestContainer> result = new LinkedList<RequestContainer>();
		
		if (pmItem.item != null) {
			pmItem.item.stream().map(this::convertToDomain).forEach(result::addAll);
		} else {
			RestRequestContainer request = new RestRequestContainer(pmItem.name, pmItem.request.url.raw, pmItem.request.method);
			
			//TODO: this should happen automatically...
			request.setId(UUID.randomUUID().toString());
			
			//adding headers
			RestHeaderAspect headers = new RestHeaderAspect();
			pmItem.request.header.forEach(h -> headers.getEntries().add(new HeaderEntry(h.key, h.value, true)));
			request.addAspect(headers);
			
			//adding bodies
			RestBodyAspect body = new RestBodyAspect();
			if (pmItem.request.body != null) {
				body.setBody(pmItem.request.body.raw);
			}
			request.addAspect(body);
			result.add(request);
		}
		
		
		return result;
	}

	public Environment importEnvironment(String json) throws Exception {
		PostmanEnvironment pmEnv = readJson(json, PostmanEnvironment.class);
		Environment resEnv = new Environment(pmEnv.name);
		for (PostmanEnvValue envValue : pmEnv.values) {
			resEnv.getEntries().add(new EnvironmentEntry(envValue.key, envValue.value, envValue.enabled));
		}
		return resEnv;
	}
}
