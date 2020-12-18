package milkman.plugin.test.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import milkman.domain.Environment;
import milkman.domain.ResponseAspect;
import reactor.core.publisher.Flux;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultEnvAspect implements ResponseAspect {

	private Environment environment;

	@Override
	public String getName() {
		return "Environment";
	}

}
