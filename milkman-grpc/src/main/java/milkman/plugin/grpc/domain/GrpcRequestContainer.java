package milkman.plugin.grpc.domain;

import lombok.*;
import milkman.domain.RequestContainer;
import org.apache.commons.lang3.StringUtils;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GrpcRequestContainer extends RequestContainer {

	private String endpoint;
	private boolean useTls;
	
	public GrpcRequestContainer(String name, String endpoint) {
		super(name);
		this.endpoint = endpoint;
	}


	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(endpoint, searchString)
				|| super.match(searchString);
	}
	
	
	@Override
	public String getType() {
		return "grpc";
	}


}
