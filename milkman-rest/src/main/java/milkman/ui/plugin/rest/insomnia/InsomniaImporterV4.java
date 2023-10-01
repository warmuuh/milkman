package milkman.ui.plugin.rest.insomnia;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.links.Link;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.plugin.UiPluginManager;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

@Slf4j
@RequiredArgsConstructor
public class InsomniaImporterV4 {

	private final List<InsomniaRequestImporter> requestImporters;


	public void importCollection(String json, List<Collection> collections, List<Environment> environments) throws Exception {
		InsomniaExportedData exportedData = loadInsomniaData(json);

		List<InsomniaWorkspace> workspaces = exportedData.getResources()
				.stream().filter(Objects::nonNull)
				.filter(InsomniaWorkspace.class::isInstance)
				.map(InsomniaWorkspace.class::cast)
				.toList();

		if (exportedData.getResources().isEmpty()) {
			throw new IllegalArgumentException("Empty Collection");
		}

		if (workspaces.isEmpty()) {
			throw new IllegalArgumentException("No Workspace found in data");
		}
		if (workspaces.size() > 1) {
			throw new IllegalArgumentException("Only single workspace can be imported");
		}

		collections.addAll(convertToDomain(workspaces.get(0)));
		environments.addAll(convertEnvironments(workspaces.get(0).getEnvironments(), null));
	}

	private List<Environment> convertEnvironments(List<InsomniaEnvironment> iEnvs, Environment parent) {
		List<Environment> envs = new LinkedList<>();
		iEnvs.forEach(iEnv -> {
			Environment env = new Environment(iEnv.getName());
			env.setId(UUID.randomUUID().toString());
			if (parent != null) {
				parent.getEntries().forEach(e -> env.setOrAdd(e.getName(), e.getValue()));
			}
			iEnv.getData().forEach((k,v) -> env.setOrAdd(k, v.toString()));
			envs.add(env);
			envs.addAll(convertEnvironments(iEnv.getSubEnvironments(), env));
		});
		return envs;
	}

	private InsomniaExportedData loadInsomniaData(String json) throws IOException {
		InsomniaExportedData exportedData = readJson(json, InsomniaExportedData.class);

		Map<String, Object> idToObj = new HashMap<>();
		exportedData.getResources()
				.stream().filter(Objects::nonNull)
				.forEach(r -> idToObj.put(r.getId(), r));

		//wire up things:
		exportedData.getResources()
				.stream().filter(Objects::nonNull)
				.forEach(r -> {
				if (r.getParentId() != null) {
					Object parent = idToObj.get(r.getParentId());

					if (r instanceof InsomniaFile && parent instanceof InsomniaWorkspace) {
						((InsomniaWorkspace) parent).addFile((InsomniaFile) r);
					} else if (r instanceof InsomniaEnvironment && parent instanceof InsomniaWorkspace) {
						((InsomniaWorkspace) parent).addEnv((InsomniaEnvironment) r);
					} else if (parent instanceof InsomniaCollection) {
							((InsomniaCollection) parent).addChild(r);
					} else if (parent instanceof InsomniaEnvironment) {
						((InsomniaEnvironment)parent).addChild((InsomniaEnvironment)r);
					}
				}
		});
		return exportedData;
	}

	private <T> T readJson(String json, Class<T> type) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		T pmCollection = mapper.readValue(json, type);
		return pmCollection;
	}

	private List<Collection> convertToDomain(InsomniaWorkspace workspace) {

		List<Collection> collections = new LinkedList<>();

		if (!workspace.getRequests().isEmpty()) {
			//create pseudo request group
			InsomniaRequestGroup rootRequests = new InsomniaRequestGroup();
			rootRequests.setName(workspace.getName());
			rootRequests.setRequests(workspace.getRequests());
			rootRequests.setRequestGroups(new LinkedList<>());
			collections.add(convertToCollection(rootRequests, workspace.getFiles()));
		}


		for(var reqGroup : workspace.getRequestGroups()) {
			collections.add(convertToCollection(reqGroup, workspace.getFiles()));
		}
		return collections;
	}

	private Collection convertToCollection(InsomniaRequestGroup reqGroup, List<InsomniaFile> files) {
		List<RequestContainer> requests = new LinkedList<>();
		List<Folder> folders = new LinkedList<>();

		reqGroup.getRequestGroups().forEach(itm -> convertToFolder(itm, files, requests, folders, null));
		reqGroup.getRequests().forEach(itm -> convertToRequest(itm, files, requests, folders, null));

		return new Collection(UUID.randomUUID().toString(), reqGroup.getName(), false, requests, folders);
	}

	private void convertToFolder(InsomniaRequestGroup reqGroup, List<InsomniaFile> files, List<RequestContainer> requests, List<Folder> folders, Folder currentFolder) {
		Folder folder = new Folder(UUID.randomUUID().toString(), reqGroup.getName(), new LinkedList<>(), new LinkedList<>());
		folders.add(folder);
		reqGroup.getRequestGroups().forEach(itm -> convertToFolder(itm, files, requests, folder.getFolders(), folder));
		reqGroup.getRequests().forEach(itm -> convertToRequest(itm, files, requests, folder.getFolders(), folder));
	}

	private void convertToRequest(InsomniaResource iReq, List<InsomniaFile> files, List<RequestContainer> requests, List<Folder> folders, Folder curFolder) {
		Optional<RequestContainer> convertedRequest = requestImporters.stream()
				.filter(importer -> importer.supportRequestType(iReq))
				.map(i -> i.convert(iReq, files))
				.findAny();

		convertedRequest.ifPresent(request -> {
			requests.add(request);
			if (curFolder != null)
				curFolder.getRequests().add(request.getId());
		});
	}

	@Data
	public static class InsomniaExportedData {
		List<InsomniaResource> resources;

	}

	@Data
	@JsonTypeInfo(use = Id.NAME,
			include = As.PROPERTY, property = "_type", visible = true, defaultImpl = Void.class)
	@JsonSubTypes({
			@JsonSubTypes.Type(value = InsomniaRequestGroup.class, name = "request_group"),
			@JsonSubTypes.Type(value = InsomniaWorkspace.class, name = "workspace"),
			@JsonSubTypes.Type(value = InsomniaHttpRequest.class, name = "request"),
			@JsonSubTypes.Type(value = InsomniaWsRequest.class, name = "websocket_request"),
			@JsonSubTypes.Type(value = InsomniaGrpcRequest.class, name = "grpc_request"),
			@JsonSubTypes.Type(value = InsomniaEnvironment.class, name = "environment"),
			@JsonSubTypes.Type(value = InsomniaCookieJar.class, name = "cookie_jar"),
			@JsonSubTypes.Type(value = InsomniaProtoFile.class, name = "proto_file"),
	})
	public static class InsomniaResource {
		@JsonProperty("_id")
		String id;
		String parentId;
		String name;
		String description;
	}


	@Data
	public static abstract class InsomniaCollection extends InsomniaResource {
		String scope;

		@JsonIgnore
		List<InsomniaRequestGroup> requestGroups = new LinkedList<>();

		@JsonIgnore
		List<InsomniaRequest> requests = new LinkedList<>();

		void addChild(InsomniaResource resource) {
			if (resource instanceof InsomniaRequestGroup) {
				requestGroups.add((InsomniaRequestGroup) resource);
			} else if (resource instanceof InsomniaRequest) {
				requests.add(((InsomniaRequest) resource));
			} else {
				log.warn("Unknown insomnia type: " + resource.getClass());
			}
		}
	}

	@Data
	public static class InsomniaWorkspace extends InsomniaCollection {
		String scope;

		@JsonIgnore
		List<InsomniaFile> files = new LinkedList<>();


		@JsonIgnore
		List<InsomniaEnvironment> environments = new LinkedList<>();

		public void addFile(InsomniaFile r) {
			files.add(r);
		}

		public void addEnv(InsomniaEnvironment r) {
			environments.add(r);
		}

	}

	@Data
	public static class InsomniaEnvironment extends InsomniaResource {
		Map<String, Object> data;

		@JsonIgnore
		List<InsomniaEnvironment> subEnvironments = new LinkedList<>();

		public void addChild(InsomniaEnvironment r) {
			subEnvironments.add(r);
		}
	}

	@Data
	public static class InsomniaCookieJar extends InsomniaResource {

	}

	@Data
	public static class InsomniaRequestGroup extends InsomniaCollection {

	}

	@Data
	public abstract static class InsomniaRequest extends InsomniaResource {

	}


	@Data
	public static class InsomniaHttpRequest extends InsomniaRequest {
		String url;
		String method;
		InsomniaRequestAuthentication authentication;
		List<InsomniaRequestHeader> headers;
		List<InsomniaRequestParameter> parameters;
		InsomniaRequestBody body;
	}

	@Data
	public static class InsomniaWsRequest extends InsomniaRequest {
		String url;
		String method;
		InsomniaRequestAuthentication authentication;
		List<InsomniaRequestHeader> headers;
		List<InsomniaRequestParameter> parameters;
	}

	@Data
	public static class InsomniaGrpcRequest extends InsomniaRequest {
		String url;
		String method;
		String protoFileId;
		String protoMethodName;
		InsomniaGrpcRequestBody body;
	}


	@Data
	public static class InsomniaRequestHeader {
		String name;
		String value;
		boolean disabled;
	}

	@Data
	public static class InsomniaGrpcRequestBody {
		String text;
	}


	public abstract static class InsomniaFile extends InsomniaResource {
		public abstract String getBody();
	}

	@Data
	public static class InsomniaProtoFile extends InsomniaFile {
		String protoText;

		@Override
		public String getBody() {
			return protoText;
		}
	}

	@Data
	public static class InsomniaRequestBody {
		String mimeType;
		String text;
	}

	@Data
	public static class InsomniaRequestParameter {
		String name;
		String value;
		boolean disabled;
	}

	@Data
	public static class InsomniaRequestAuthentication {
		String type;
		boolean disabled;
		String username;
		String password;
	}

}
