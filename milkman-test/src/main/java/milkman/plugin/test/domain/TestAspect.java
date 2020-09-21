package milkman.plugin.test.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class TestAspect extends RequestAspect {
	private String note = "";
	
	public TestAspect() {
		super("test");
	}
}
