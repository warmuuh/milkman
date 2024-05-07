package milkman.plugin.test.domain;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import milkman.domain.ResponseAspect;
import milkman.domain.StatusInfoContainer;
import reactor.core.publisher.Flux;

import java.util.Map;

import static milkman.domain.ResponseContainer.StyledText;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultAspect implements ResponseAspect {

	private Flux<TestResultEvent> results;

	@Override
	public String getName() {
		return "Results";
	}



	public enum TestResultState {
		STARTED, SUCCEEDED, FAILED, EXCEPTION, IGNORED, SKIPPED
	}

	@Value
	public static class TestResultEvent {
		String requestId;
		String requestName;
		TestResultState resultState;
		StatusInfoContainer details;
		String error;

		public Optional<StatusInfoContainer> getDetails() {
			return Optional.ofNullable(details);
		}

		public Optional<String> getError() {
			return Optional.ofNullable(error);
		}
	}


}
