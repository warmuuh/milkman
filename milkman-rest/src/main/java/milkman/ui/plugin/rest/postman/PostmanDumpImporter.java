package milkman.ui.plugin.rest.postman;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.postman.dump.PostmanDump;
import milkman.ui.plugin.rest.postman.dump.PostmanDump.PostmanCollection;
import milkman.ui.plugin.rest.postman.dump.PostmanDump.PostmanRequest;

public class PostmanDumpImporter {

	public Workspace importDump(InputStream json) throws Exception {
		PostmanDump pmDump = readJson(json, PostmanDump.class);
		
		
		List<Collection> collections = new LinkedList<Collection>();
		if (pmDump.getCollections() != null) {
			for (PostmanDump.PostmanCollection postmanCollection : pmDump.getCollections()) {
				collections.add(convertToDomain(postmanCollection));
			}
		}
		
		List<Environment> environments = new LinkedList<Environment>();
		if (pmDump.getEnvironments() != null) {
			for (PostmanDump.PostmanEnvironment postmanEnv : pmDump.getEnvironments()) {
				environments.add(convertToDomain(postmanEnv));
			}
		}
		
		
		Workspace workspace = new Workspace(0, "", "", collections, Collections.emptyList(), null);
		workspace.getEnvironments().addAll(environments);
		return workspace;
	}
	
	
	
	
	private Environment convertToDomain(PostmanDump.PostmanEnvironment postmanEnv) {
		Environment result = new Environment(postmanEnv.getName());
		postmanEnv.getValues().forEach(e -> result.getEntries().add(new EnvironmentEntry(e.getKey(), e.getValue(), e.isEnabled())));
		return result;
	}




	private Collection convertToDomain(PostmanCollection postmanCollection) {
		Collection result = new Collection();
		result.setName(postmanCollection.getName());
		result.setRequests(new LinkedList<RequestContainer>());
		if (postmanCollection.getRequests() != null) {
			for (PostmanRequest request : postmanCollection.getRequests()) {
				result.getRequests().add(convertToDomain(request));
			}
		}
		
		return result;
	}

	
	private RequestContainer convertToDomain(PostmanDump.PostmanRequest request) {
		RestRequestContainer container = new RestRequestContainer(request.getName(), request.getUrl(), request.getMethod());

		//TODO: this should happen automatically...
		container.setId(UUID.randomUUID().toString());
		container.setInStorage(true);
		
		
		RestHeaderAspect headers = new RestHeaderAspect();
		if (request.getHeaderData() != null)
			request.getHeaderData().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), h.getKey(), h.getValue(), true)));
		container.addAspect(headers);
		
		RestBodyAspect body = new RestBodyAspect();
		if (request.getRawModeData() != null)
			body.setBody(request.getRawModeData());
		container.addAspect(body);
		
		return container;
	}


	private <T> T readJson(InputStream json, Class<T> type) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		T pmCollection = mapper.readValue(json, type);
		return pmCollection;
	}

	
}
