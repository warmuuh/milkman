package milkman.ui.plugin.rest.postman.importers;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkman.rest.postman.schema.v21.ItemGroup;
import com.milkman.rest.postman.schema.v21.PostmanCollection210;

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

public class PostmanImporterV21 {

	
	public Collection importCollection(String json) throws Exception {
		PostmanCollection210 pmCollection = readJson(json, PostmanCollection210.class);
		
		if (pmCollection.getItem().isEmpty()) {
			throw new IllegalArgumentException("Empty Collection");
		}
		
		return convertToDomain(pmCollection);
	}


	public Environment importEnvironment(String json) throws Exception {
		PostmanEnvironment pmEnv = readJson(json, PostmanEnvironment.class);
		if (pmEnv.values == null || pmEnv.values.isEmpty()) {
			throw new IllegalArgumentException("Empty Environment");
		}
		
		return convertToDomain(pmEnv);
	}




	private Environment convertToDomain(PostmanEnvironment pmEnv) {
		Environment resEnv = new Environment(pmEnv.name);
		for (PostmanEnvValue envValue : pmEnv.values) {
			resEnv.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(), envValue.key, envValue.value, envValue.enabled));
		}
		return resEnv;
	}
	
	
	private <T> T readJson(String json, Class<T> type) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		T pmCollection = mapper.readValue(json, type);
		return pmCollection;
	}

	private Collection convertToDomain(PostmanCollection210 pmCollection) {
		//flattening folders:
		List<RequestContainer> requests = new LinkedList<>();
		for(ItemGroup container : pmCollection.getItem()) {
			requests.addAll(convertToDomain(container));	
		}

		return new Collection(UUID.randomUUID().toString(), pmCollection.getInfo().getName(), false, requests);
	}
	
	
	private List<RequestContainer> convertToDomain(ItemGroup pmItem) {
		List<RequestContainer> result = new LinkedList<RequestContainer>();
		
		if (pmItem.getItem() != null) {
			pmItem.getItem().stream().map(this::convertToDomain).forEach(result::addAll);
		} else {
			RestRequestContainer request = new RestRequestContainer(pmItem.getName(), pmItem.getRequest().getUrl().getRaw(), pmItem.getRequest().getMethod().toString());
			
			//TODO: this should happen automatically...
			request.setId(UUID.randomUUID().toString());
			request.setInStorage(true);
			
			//adding headers
			RestHeaderAspect headers = new RestHeaderAspect();
			pmItem.getRequest().getHeader().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(),h.getKey(), h.getValue(), true)));
			request.addAspect(headers);
			
			//adding bodies
			RestBodyAspect body = new RestBodyAspect();
			if (pmItem.getRequest().getBody() != null) {
				body.setBody(pmItem.getRequest().getBody().getRaw());
			}
			request.addAspect(body);
			result.add(request);
		}
		
		
		return result;
	}

}
