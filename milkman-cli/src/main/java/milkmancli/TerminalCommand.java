package milkmancli;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import lombok.Data;

public abstract class TerminalCommand implements Callable<Void>{

	private List<Parameter> parameters;
	private List<Option> options;

	public abstract String getName();
	public abstract String getAlias();
	public abstract String getDescription();
	protected List<Parameter> createParameters(){
		return Collections.emptyList();
	};
	
	protected List<Option> createOptions(){
		return Collections.emptyList();
	};
	
	
	protected Optional<String> tryGetParameterValue(String paramName) {
		return parameters.stream()
			.filter(p -> p.getName().equals(paramName))
			.findAny()
			.flatMap(p -> Optional.ofNullable(p.getValue()));
	}
	
	protected String getParameterValue(String paramName) {
		return tryGetParameterValue(paramName)
			.orElseThrow(() -> new IllegalArgumentException("Parameter with name " + paramName + " not found"));
	}
	
	protected boolean isOption(String optionName) {
		return options.stream()
			.filter(o -> o.getName().equals(optionName))
			.map(p -> p.isValue())
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("Option with name " + optionName + " not found"));
	}
	
	
	public List<Parameter> getParameters(){
		if (parameters == null) {
			this.parameters = createParameters();
		}
		return parameters;
	}
	
	public List<Option> getOptions(){
		if (options == null) {
			options = createOptions();
		}
		return options;
	}
	
	
	@Data
	public static class Parameter {
		private final String name;
		private final String description;
		private final Completion completion;
		private boolean required;
		String value;
		
		public Parameter(String name, String description, Completion completion, boolean required) {
			super();
			this.name = name;
			this.description = description;
			this.completion = completion;
			this.required = required;
		}

		public Parameter(String name, String description, Completion completion) {
			super();
			this.name = name;
			this.description = description;
			this.completion = completion;
		}
		
		
		
	}
	
	@Data
	public static class Option {
		private final String name;
		private final String alias;
		private final String description;
		boolean value = false;
	}
	
	
	public interface Completion extends Iterable<String> {
		
		Collection<String> getCompletionCandidates();
		
		@Override
		default Iterator<String> iterator() {
			return getCompletionCandidates().iterator();
		}
		
	}
}
