package milkman.plugin.scripting;

import java.util.Optional;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import milkman.domain.Environment;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseAspect;
import milkman.domain.ResponseContainer;

@Data
public class MilkmanScriptingFacade {

	@RequiredArgsConstructor
	public static class MilkmanResponseFacade extends jdk.nashorn.api.scripting.AbstractJSObject {
		private final ResponseContainer response;

		@Override
		public Object getMember(String name) {
			Optional<ResponseAspect> aspect = findAspectByName(name);
			if (aspect.isPresent())
				return aspect.get();
			return super.getMember(name);
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
	
	public MilkmanScriptingFacade(ResponseContainer response, RequestExecutionContext context) {
		this.response = new MilkmanResponseFacade(response);
		activeEnv = context.getActiveEnvironment();
	}
	
	public void setEnvironmentVariable(String varName, String varValue) {
		activeEnv.ifPresent(e -> e.setOrAdd(varName, varValue));
	}
	
}
