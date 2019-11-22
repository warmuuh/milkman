package milkman.plugin.grpc.domain;

import lombok.Data;
import milkman.domain.ResponseContainer;

@Data
public class GrpcResponseContainer extends ResponseContainer {

	private final String endpoint;
}
