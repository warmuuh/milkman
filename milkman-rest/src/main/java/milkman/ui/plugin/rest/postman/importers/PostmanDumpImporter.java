package milkman.ui.plugin.rest.postman.importers;

import java.io.IOException;
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
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.postman.dump.PostmanDump;
import milkman.ui.plugin.rest.postman.dump.PostmanDump.PostmanCollection;
import milkman.ui.plugin.rest.postman.dump.PostmanDump.PostmanFolder;
import milkman.ui.plugin.rest.postman.dump.PostmanDump.PostmanRequest;

public class PostmanDumpImporter {

	public Workspace importDump(String json) throws Exception {
		PostmanDump pmDump = readJson(json, PostmanDump.class);
		
		//validation
		if (pmDump.getCollections() == null && pmDump.getEnvironments() == null && pmDump.getGlobals() == null) {
			throw new IllegalArgumentException("Empty Dump data");
		}
		
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
		if (pmDump.getGlobals() != null && pmDump.getGlobals().size() > 0) {
			environments.add(convertGlobalsToDomain(pmDump.getGlobals()));
		}
		
		
		Workspace workspace = new Workspace(0, "", "", collections, Collections.emptyList(), null);
		workspace.getEnvironments().addAll(environments);
		return workspace;
	}
	
	
	
	
	private Environment convertToDomain(PostmanDump.PostmanEnvironment postmanEnv) {
		Environment result = new Environment(postmanEnv.getName());
		result.setGlobal(postmanEnv.getName().toLowerCase().contains("globals"));
		postmanEnv.getValues().forEach(e -> result.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(), e.getKey(), e.getValue(), e.isEnabled())));
		return result;
	}

	private Environment convertGlobalsToDomain(List<PostmanDump.EnvironmentEntry> globals) {
		Environment result = new Environment("Globals");
		result.setGlobal(true);
		globals.forEach(e -> result.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(), e.getKey(), e.getValue(), e.isEnabled())));
		return result;
	}



	private Collection convertToDomain(PostmanCollection postmanCollection) {
		List<RequestContainer> requests = new LinkedList<RequestContainer>();
		if (postmanCollection.getRequests() != null) {
			for (PostmanRequest request : postmanCollection.getRequests()) {
				requests.add(convertToDomain(request));
			}
		}
		

		List<Folder> folders = new LinkedList<Folder>();
		if (postmanCollection.getFolders() != null) {
			for (PostmanFolder postmanFolder : postmanCollection.getFolders()) {
				folders.add(convertToDomain(postmanFolder));
			}
		}
		folders.sort((a,b) -> postmanCollection.getFoldersOrder().indexOf(a.getId()) - postmanCollection.getFoldersOrder().indexOf(b.getId()));

		
		return new Collection(UUID.randomUUID().toString(), postmanCollection.getName(), false, requests, folders);
	}

	
	private Folder convertToDomain(PostmanFolder postmanFolder) {

		List<String> requests = new LinkedList<String>();
		if (postmanFolder.getRequests() != null)
			requests.addAll(postmanFolder.getRequests());
		
		
		List<Folder> subfolders = new LinkedList<Folder>();
		if (postmanFolder.getFolders() != null) {
			for (PostmanFolder postmanSubFolder : postmanFolder.getFolders()) {
				subfolders.add(convertToDomain(postmanSubFolder));
			}
		}
		
		subfolders.sort((a,b) -> postmanFolder.getFoldersOrder().indexOf(a.getId()) - postmanFolder.getFoldersOrder().indexOf(b.getId()));
		
		return new Folder(postmanFolder.getId(), postmanFolder.getName(), subfolders, requests);
	}




	private RequestContainer convertToDomain(PostmanDump.PostmanRequest request) {
		RestRequestContainer container = new RestRequestContainer(request.getName(), request.getUrl(), request.getMethod());
		container.setId(request.getId());
		container.setInStorage(true);
		
		
		RestHeaderAspect headers = new RestHeaderAspect();
		if (request.getHeaderData() != null)
			request.getHeaderData().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), h.getKey(), h.getValue(), true)));
		container.addAspect(headers);
		
		RestBodyAspect body = new RestBodyAspect();
		if (request.getRawModeData() != null)
			body.setBody(request.getRawModeData());
		else if (request.getData() != null && request.getData() instanceof String)
			body.setBody(request.getData().toString());
		container.addAspect(body);
		
		return container;
	}


	private <T> T readJson(String json, Class<T> type) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		T pmCollection = mapper.readValue(json, type);
		return pmCollection;
	}

	
}
