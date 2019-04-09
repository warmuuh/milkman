package milkman.ui.plugin.rest.postman.dump;

import java.util.List;

import lombok.Data;

@Data
public class PostmanDump {
	List<PostmanCollection> collections;
	List<PostmanEnvironment> environments;
	
	@Data
	public static class PostmanCollection {
		String name;
		List<PostmanRequest> requests;
	}
	
	@Data
	public static class PostmanRequest {
		String name;
		String url;
		String method;
		List<HeaderDataEntry> headerData;
		String rawModeData;
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
