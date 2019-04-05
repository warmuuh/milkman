package milkman.ui.plugin.rest.postman;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.poynt.postman.model.PostmanCollection;
import co.poynt.postman.model.PostmanContainer;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class PostmanImporter {

	public Collection importString(String json) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		PostmanCollection pmCollection = mapper.readValue(json, PostmanCollection.class);
		return convertToDomain(pmCollection);
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
}
