package co.poynt.postman.model;

public class PostmanEnvValue {
	public String key;
	public String value;
	public String type;
	public String name;
	public boolean enabled;
	
	@Override
	public String toString() {
		return "["+key+":"+value+"]";
	}
}
