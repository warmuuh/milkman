package milkman.plugin.grpc.domain;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.domain.ResponseAspect;
import milkman.utils.json.BlockingFluxByteToStringConverter;
import reactor.core.publisher.Flux;

@Data
@AllArgsConstructor
public class GrpcResponsePayloadAspect implements ResponseAspect {

	@JsonSerialize(converter = BlockingFluxByteToStringConverter.class)
	private Flux<byte[]> payloads;

	
	@Override
	public String getName() {
		return "Payload";
	}
}
