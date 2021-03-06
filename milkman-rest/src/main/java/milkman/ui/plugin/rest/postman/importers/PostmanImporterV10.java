package milkman.ui.plugin.rest.postman.importers;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkman.rest.postman.schema.v1.PostmanCollection100;
import com.milkman.rest.postman.schema.v1.Request;

import milkman.domain.Collection;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class PostmanImporterV10 {

	
	public Collection importCollection(String json) throws Exception {
		PostmanCollection100 pmCollection = readJson(json, PostmanCollection100.class);
		
		if (pmCollection.getRequests().isEmpty()) {
			throw new IllegalArgumentException("Empty collection");
		}
		
		return convertToDomain(pmCollection);
	}

	
	
	private <T> T readJson(String json, Class<T> type) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		T pmCollection = mapper.readValue(json, type);
		return pmCollection;
	}

	private Collection convertToDomain(PostmanCollection100 pmCollection) {
		//ignoring folders
		List<RequestContainer> requests = new LinkedList<>();
		for(Request container : pmCollection.getRequests()) {
			requests.addAll(convertToDomain(container));	
		}
		
		
//		postman v1.0 doesnt seem to support subfolders, so we just return a list of all folders
		List<Folder> folders = new LinkedList<>();
		if (pmCollection.getFolders() != null) {
			for (com.milkman.rest.postman.schema.v1.Folder postmanFolder : pmCollection.getFolders()) {
				var folder = convertToDomain(postmanFolder);
				folders.add(folder);
			}
		}
		return new Collection(UUID.randomUUID().toString(), pmCollection.getName(), false, requests, folders);
	}
	
	private Folder convertToDomain(com.milkman.rest.postman.schema.v1.Folder postmanFolder) {
		
		List<String> requests = new LinkedList<>();
		if (postmanFolder.getOrder() != null)
			requests.addAll(postmanFolder.getOrder());
		
		return new Folder(postmanFolder.getId(), postmanFolder.getName(), new LinkedList(), requests);
	}


	private List<RequestContainer> convertToDomain(Request pmItem) {
		List<RequestContainer> result = new LinkedList<RequestContainer>();
		
		RestRequestContainer request = new RestRequestContainer(pmItem.getName(), pmItem.getUrl(), pmItem.getMethod().toString());
		
		request.setId(pmItem.getId());
		request.setInStorage(true);
		
		//adding headers
		RestHeaderAspect headers = new RestHeaderAspect();
		pmItem.getHeaderData().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(),h.getKey(), h.getValue(), true)));
		request.addAspect(headers);
		
		//adding bodies
		RestBodyAspect body = new RestBodyAspect();
		if (pmItem.getRawModeData() != null) {
			body.setBody(pmItem.getRawModeData());
		}
		request.addAspect(body);
		result.add(request);
		
		
		return result;
	}

}
