package milkman.plugin.grpc.domain;



import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.domain.ResponseAspect;
import reactor.core.publisher.Flux;

@Data
@AllArgsConstructor
public class GrpcResponsePayloadAspect implements ResponseAspect {
	
	private Flux<String> payloads;

	
	@Override
	public String getName() {
		return "Payload";
	}
}
