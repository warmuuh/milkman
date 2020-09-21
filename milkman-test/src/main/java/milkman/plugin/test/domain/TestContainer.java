package milkman.plugin.test.domain;

import milkman.domain.RequestContainer;

public class TestContainer extends RequestContainer {
	@Override
	public String getType() {
		return "TEST";
	}
}
