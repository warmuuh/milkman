package milkman.plugin.grpc.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class GrpcOperationAspect extends RequestAspect {

	String operation;

	public GrpcOperationAspect() {
		super("operation");
	}
	
}
