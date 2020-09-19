package milkman.plugin.grpc.domain;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.domain.ResponseAspect;
import milkman.utils.json.BlockingFluxToByteConverter;
import reactor.core.publisher.Flux;

@Data
@AllArgsConstructor
public class GrpcResponsePayloadAspect implements ResponseAspect {

	@JsonSerialize(converter = BlockingFluxToByteConverter.class)
	private Flux<String> payloads;

	
	@Override
	public String getName() {
		return "Payload";
	}
}
