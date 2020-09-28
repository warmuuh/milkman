package milkman.plugin.test.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.RequestAspect;

import java.util.LinkedList;
import java.util.List;

@Data
public class TestAspect extends RequestAspect {
	private List<TestDetails> requests = new LinkedList<>();
	private boolean stopOnFirstFailure = false;

	public TestAspect() {
		super("test");
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TestDetails {
		private String id;
		private boolean skip;
	}
}
