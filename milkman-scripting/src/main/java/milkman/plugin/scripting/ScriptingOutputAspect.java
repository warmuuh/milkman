package milkman.plugin.scripting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.ResponseAspect;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptingOutputAspect implements ResponseAspect {
	private String preScriptOutput = "";
	private String postScriptOutput = "";

	@Override
	public String getName() {
		return "Script Output";
	}
}
