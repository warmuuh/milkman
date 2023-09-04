package milkman.plugin.scripting.nashorn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import milkman.domain.*;
import milkman.ui.main.Toaster;

import java.util.Map;
import java.util.Optional;

@Data
public class MilkmanNashornFacade {


	private final MilkmanRequestFacade request;
	private final MilkmanResponseFacade response;
	private final Optional<Environment> activeEnv;
	private RequestExecutionContext context;
	private final Toaster toaster;

	public MilkmanNashornFacade(RequestContainer request, ResponseContainer response, RequestExecutionContext context, Toaster toaster) {
		this.response = response != null ? new MilkmanResponseFacade(response) : null;
		this.request = request != null ? new MilkmanRequestFacade(request) : null;
		activeEnv = context.getActiveEnvironment();
		this.context = context;
		this.toaster = toaster;
	}

	public void toast(String text){
		toaster.showToast(text);
	}

	public void setEnvironmentVariable(String varName, String varValue) {
		activeEnv.ifPresent(e -> e.setOrAdd(varName, varValue));
	}

	public String getEnvironmentVariable(String varName) {
		return context.lookupValue(varName).orElse(null);
	}

	@RequiredArgsConstructor
	public static class MilkmanResponseFacade extends org.openjdk.nashorn.api.scripting.AbstractJSObject {
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


	@RequiredArgsConstructor
	public static class MilkmanRequestFacade extends org.openjdk.nashorn.api.scripting.AbstractJSObject {
		private final RequestContainer request;
		private final ObjectMapper mapper = new ObjectMapper();
		@Override
		public Object getMember(String name) {
			Optional<RequestAspect> aspect = findAspectByName(name);
			if (aspect.isPresent())
				return convertToMap(aspect);
			return super.getMember(name);
		}

		private Map<String, Object> convertToMap(Optional<RequestAspect> aspect) {
			return mapper.convertValue(aspect.get(), new TypeReference<Map<String, Object>>() {});
		}

		private Optional<RequestAspect> findAspectByName(String name) {
			Optional<RequestAspect> aspect = request.getAspects().stream().filter(a -> a.getName().equals(name)).findAny();
			return aspect;
		}

		@Override
		public boolean hasMember(String name) {
			Optional<RequestAspect> aspect = findAspectByName(name);
			if (aspect.isPresent())
				return true;
			return super.hasMember(name);
		}

		@Override
		public void setMember(String name, Object value) {
			super.setMember(name, value);
		}
	}

}
