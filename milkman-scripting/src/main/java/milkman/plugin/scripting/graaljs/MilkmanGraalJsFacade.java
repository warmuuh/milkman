package milkman.plugin.scripting.graaljs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import milkman.domain.*;
import milkman.ui.main.Toaster;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public class MilkmanGraalJsFacade {


	private final MilkmanRequestFacade request;
	private final MilkmanResponseFacade response;
	private final Optional<Environment> activeEnv;
	private RequestExecutionContext context;
	private final Toaster toaster;

	public MilkmanGraalJsFacade(RequestContainer request, ResponseContainer response, RequestExecutionContext context, Toaster toaster) {
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
	public static class MilkmanResponseFacade implements ProxyObject {
		private final ResponseContainer response;
		private final ObjectMapper mapper = new ObjectMapper();

		@Override
		public Object getMember(String name) {
			Optional<ResponseAspect> aspect = findAspectByName(name);
			if (aspect.isPresent()){
				//deep copy so that e.g. streams are converted to a string property
				return DynamicReadonlyProxy.from(convertToMap(aspect));
			}
			return null;
		}

		@Override
		public Object getMemberKeys() {
			return response.getAspects().stream().map(ResponseAspect::getName).collect(Collectors.toList());
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
			return findAspectByName(name).isPresent();
		}

		@Override
		public void putMember(String key, Value value) {
			throw new UnsupportedOperationException("Adding new aspects is not supported");
		}

	}


	@RequiredArgsConstructor
	public static class MilkmanRequestFacade implements ProxyObject {
		private final RequestContainer request;
		private final ObjectMapper mapper = new ObjectMapper();
		@Override
		public Object getMember(String name) {
			Optional<RequestAspect> aspect = findAspectByName(name);
			//no deep copy to allow modifications
			return aspect.orElse(null);
		}

		@Override
		public Object getMemberKeys() {
			return request.getAspects().stream().map(RequestAspect::getName).collect(Collectors.toList());
		}

		private Optional<RequestAspect> findAspectByName(String name) {
			Optional<RequestAspect> aspect = request.getAspects().stream().filter(a -> a.getName().equals(name)).findAny();
			return aspect;
		}

		@Override
		public boolean hasMember(String name) {
			return findAspectByName(name).isPresent();
		}

		@Override
		public void putMember(String key, Value value) {
			throw new UnsupportedOperationException("Adding new aspects is not supported");
		}
	}
	
}
