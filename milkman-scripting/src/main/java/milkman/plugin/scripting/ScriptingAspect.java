package milkman.plugin.scripting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class ScriptingAspect extends RequestAspect {
	private String postRequestScript = "";
	private String preRequestScript = "";

	@JsonIgnore
	private String preScriptOutput = "";
	
	public ScriptingAspect() {
		super("script");
	}
}
