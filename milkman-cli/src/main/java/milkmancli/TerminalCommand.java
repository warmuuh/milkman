package milkmancli;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.Data;

public abstract class TerminalCommand implements Callable<Void>{

	private List<Parameter> parameters;

	public abstract String getName();
	public abstract String getAlias();
	public abstract String getDescription();
	protected abstract List<Parameter> createParameters();
	
	
	protected String getParameterValue(String paramName) {
		return parameters.stream()
			.filter(p -> p.getName().equals(paramName))
			.map(p -> p.getValue())
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("Parameter with name " + paramName + " not found"));
	}
	
	public List<Parameter> getParameters(){
		if (parameters == null) {
			this.parameters = createParameters();
		}
		return parameters;
	}
	
	@Data
	public static class Parameter {
		private final String name;
		private final String description;
		private final Completion completion;
		String value;
	}
	
	public interface Completion extends Iterable<String> {
		
		Collection<String> getCompletionCandidates();
		
		@Override
		default Iterator<String> iterator() {
			return getCompletionCandidates().iterator();
		}
		
	}
}
