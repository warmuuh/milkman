package milkman.ui.main;

import java.util.UUID;

import milkman.domain.RequestContainer;

public class TestRequestContainer extends RequestContainer {

	
	public TestRequestContainer(String name) {
		super(name);
		setId(UUID.randomUUID().toString());
	}

	@Override
	public String getType() {
		return "TEST";
	}

}
