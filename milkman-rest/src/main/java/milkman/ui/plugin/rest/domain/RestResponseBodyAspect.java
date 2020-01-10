package milkman.ui.plugin.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.ResponseAspect;
import milkman.utils.json.BlockingFluxToStringConverter;
import reactor.core.publisher.Flux;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponseBodyAspect implements ResponseAspect {

	@JsonSerialize(converter = BlockingFluxToStringConverter.class)
	private Flux<String> body;

	@Override
	public String getName() {
		return "body";
	}
	
}
