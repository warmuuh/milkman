package milkman.plugin.test.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

import java.util.LinkedList;
import java.util.List;

@Data
public class TestAspect extends RequestAspect {
	private List<String> requests = new LinkedList<>();
	
	public TestAspect() {
		super("test");
	}
}
