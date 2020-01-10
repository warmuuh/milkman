package milkman.plugin.scripting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import milkman.domain.Environment;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;
import milkman.ui.main.Toaster;

import java.util.Map;
import java.util.Optional;

@Data
public class MilkmanScriptingFacade {

	@RequiredArgsConstructor
	public static class MilkmanResponseFacade extends jdk.nashorn.api.scripting.AbstractJSObject {
		private final ResponseContainer response;
		private final ObjectMapper mapper = new ObjectMapper();
		@Override
		public Object getMember(String name) {
			Optional<ResponseAspect> aspect = findAspectByName(name);
			if (aspect.isPresent())
				return convertToMap(aspect);
			return super.getMember(name);
		}

		private Map<String, Object> convertToMap(Optional<ResponseAspect> aspect) {
			return mapper.convertValue(aspect.get(), new TypeReference<Map<String, Object>>() {});
		}

		private Optional<ResponseAspect> findAspectByName(String name) {
			Optional<ResponseAspect> aspect = response.getAspects().stream().filter(a -> a.getName().equals(name)).findAny();
			return aspect;
		}

		@Override
		public boolean hasMember(String name) {
			Optional<ResponseAspect> aspect = findAspectByName(name);
			if (aspect.isPresent())
				return true;
			return super.hasMember(name);
		}

		@Override
		public void setMember(String name, Object value) {
			super.setMember(name, value);
		}
	}
	
	private final MilkmanResponseFacade response;
	private final Optional<Environment> activeEnv;
	private final Toaster toaster;

	public MilkmanScriptingFacade(ResponseContainer response, RequestExecutionContext context, Toaster toaster) {
		this.response = new MilkmanResponseFacade(response);
		activeEnv = context.getActiveEnvironment();
		this.toaster = toaster;
	}
	
	public void setEnvironmentVariable(String varName, String varValue) {
		activeEnv.ifPresent(e -> e.setOrAdd(varName, varValue));
	}
	
}
