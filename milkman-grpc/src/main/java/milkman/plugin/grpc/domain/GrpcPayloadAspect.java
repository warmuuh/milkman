package milkman.plugin.grpc.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class GrpcPayloadAspect extends RequestAspect {

	private String payload;

	public GrpcPayloadAspect() {
		super("payload");
	}
	
	
}
