package milkman.domain;

import java.util.Optional;

import lombok.Data;

@Data
public class RequestExecutionContext {

	private final Optional<Environment> activeEnvironment;
	
}
