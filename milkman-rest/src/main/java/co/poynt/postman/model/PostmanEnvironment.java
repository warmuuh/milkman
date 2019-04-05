package co.poynt.postman.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostmanEnvironment {
	public String id;
	public String name;
	public List<PostmanEnvValue> values;
	public Long timestamp;
	public Boolean synced;
	
	public Map<String, PostmanEnvValue> lookup = new HashMap<String, PostmanEnvValue>();
	
	public void init() {
		for (PostmanEnvValue val : values) {
			lookup.put(val.key, val);
		}
	}
	
	public void setEnvironmentVariable(String key, String value) {
		PostmanEnvValue existingVar = this.lookup.get(key);
		if (existingVar != null) {
			//Update existing value if any
			existingVar.value = value;
		} else {
			PostmanEnvValue newVar = new PostmanEnvValue();
			newVar.key = key;
			newVar.name = "RUNTIME-" + key;
			newVar.type = "text";
			newVar.value = value;
			this.lookup.put(key,  newVar);
		}
	}
}
