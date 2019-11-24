package milkman.plugin.grpc.domain;



import java.util.concurrent.Flow.Publisher;

import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.domain.ResponseAspect;

@Data
@AllArgsConstructor
public class GrpcResponsePayloadAspect implements ResponseAspect {
	
	private Publisher<String> payloads;

	
	@Override
	public String getName() {
		return "Payload";
	}
}
