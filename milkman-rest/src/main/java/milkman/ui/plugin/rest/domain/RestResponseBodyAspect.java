package milkman.ui.plugin.rest.domain;

import java.util.concurrent.Flow.Publisher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.ResponseAspect;
import reactor.core.publisher.Flux;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponseBodyAspect implements ResponseAspect {

	private Flux<String> body;

	@Override
	public String getName() {
		return "body";
	}
	
}
