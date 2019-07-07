package milkman.ui.plugin.rest.postman.dump;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PostmanDump {
	List<PostmanCollection> collections;
	List<PostmanEnvironment> environments;
	List<EnvironmentEntry> globals;
	
	@Data
	public static class PostmanCollection {
		String name;
		List<PostmanRequest> requests;
		
		List<PostmanFolder> folders;
		
		@JsonProperty("folders_order")
		List<String> foldersOrder;
	}
	
	@Data
	public static class PostmanFolder {
		String id;
		String name;
		
		@JsonProperty("order")
		List<String> requests;
		
		List<PostmanFolder> folders;
		
		@JsonProperty("folders_order")
		List<String> foldersOrder;
	}
	
	@Data
	public static class PostmanRequest {
		String id;
		String name;
		String url;
		String method;
		List<HeaderDataEntry> headerData;
		String rawModeData;
		Object data; //can be an array or a string
	}
	
	@Data
	public static class HeaderDataEntry {
		String key;
		String value;
	}
	
	@Data
	public static class PostmanEnvironment {
		String name;
		List<EnvironmentEntry> values;
		
	}
	
	@Data
	public static class EnvironmentEntry {
		String key;
		String value;
		boolean enabled;
	}
	
}
