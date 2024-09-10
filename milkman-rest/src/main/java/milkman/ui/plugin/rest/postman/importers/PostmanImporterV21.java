package milkman.ui.plugin.rest.postman.importers;

import co.poynt.postman.model.PostmanEnvValue;
import co.poynt.postman.model.PostmanEnvironment;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.milkman.rest.postman.schema.v21.ItemGroup;
import com.milkman.rest.postman.schema.v21.PostmanCollection210;
import com.milkman.rest.postman.schema.v21.Url;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.domain.Folder;
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
		mapper.registerModule(new Postman21Module());
		T pmCollection = mapper.readValue(json, type);
		return pmCollection;
	}

	private Collection convertToDomain(PostmanCollection210 pmCollection) {
		List<RequestContainer> requests = new LinkedList<>();
		List<Folder> folders = new LinkedList<>();
		for(ItemGroup container : pmCollection.getItem()) {
			convertToDomain(container, requests, folders, null);	
		}

		return new Collection(UUID.randomUUID().toString(), pmCollection.getInfo().getName(), false, requests, folders);
	}
	
	
	private void convertToDomain(ItemGroup pmItem, List<RequestContainer> requests, List<Folder> folders, Folder curFolder) {
		
		if (pmItem.getItem() != null) {
			//it is a folder
			Folder folder = new Folder(UUID.randomUUID().toString(), pmItem.getName(), new LinkedList<>(), new LinkedList<>());
			folders.add(folder);
			pmItem.getItem().forEach(itm -> convertToDomain(itm, requests, folder.getFolders(), folder));
		} else {
			//it is a request
			RestRequestContainer request = new RestRequestContainer(pmItem.getName(), pmItem.getRequest().getUrl().getRaw(), pmItem.getRequest().getMethod().toString());
			
			//TODO: this should happen automatically...
			request.setId(UUID.randomUUID().toString());
			request.setInStorage(true);
			
			//adding headers
			RestHeaderAspect headers = new RestHeaderAspect();
			if (pmItem.getRequest().getHeader() != null) {
				pmItem.getRequest().getHeader().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(),h.getKey(), h.getValue(), true)));
			}
			request.addAspect(headers);
			
			//adding bodies
			RestBodyAspect body = new RestBodyAspect();
			if (pmItem.getRequest().getBody() != null) {
				body.setBody(pmItem.getRequest().getBody().getRaw());
			}
			request.addAspect(body);
			requests.add(request);
			if (curFolder != null)
				curFolder.getRequests().add(request.getId());
			
		}
	}
	
	public static class Postman21Module extends SimpleModule {
		
		public Postman21Module() {
			super("postman", new Version(2, 1, 0, ""));
			this.addValueInstantiator(Url.class, new UrlInstantiator());
		}

		/**
		 * the URL can be a string or an object, we have to support both here
		 */
		public class UrlInstantiator extends ValueInstantiator {
			
			@Override
			public boolean canCreateUsingDefault() {
				return true;
			}

			@Override
			public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
				return new Url();
			}

			@Override
			public boolean canCreateFromString() {
				return true;
			}

			@Override
			public Object createFromString(DeserializationContext ctxt, String value) throws IOException {
				Url result = new Url();
				result.setRaw(value);
				return result;
			}
			
		}
		
	}
	
	

}
