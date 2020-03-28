package milkman.plugin.scripting;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class ScriptingAspect extends RequestAspect {
	private String postRequestScript = "";
	private String preRequestScript = "";
	
	public ScriptingAspect() {
		super("script");
	}
}
