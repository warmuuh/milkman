package milkman.plugin.test.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import milkman.domain.RequestContainer;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TestContainer extends RequestContainer {


	public TestContainer(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "TEST";
	}
}
