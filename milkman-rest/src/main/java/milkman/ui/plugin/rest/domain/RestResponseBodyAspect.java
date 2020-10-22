package milkman.ui.plugin.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.ResponseAspect;
import milkman.utils.json.BlockingFluxByteToStringConverter;
import reactor.core.publisher.Flux;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponseBodyAspect implements ResponseAspect {

	@JsonSerialize(converter = BlockingFluxByteToStringConverter.class)
	private Flux<byte[]> body;

	@Override
	public String getName() {
		return "body";
	}
	
}
