package milkman.domain;

import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public class RequestExecutionContext {

	private final Optional<Environment> activeEnvironment;

	private final List<Environment> globalEnvironments;

	public Optional<String> lookupValue(String key) {
		return Stream.concat(activeEnvironment.stream(), globalEnvironments.stream())
				.flatMap(env -> env.getEntries().stream())
				.filter(entry -> entry.getName().equals(key))
				.map(Environment.EnvironmentEntry::getValue)
				.findFirst();
	}
}
