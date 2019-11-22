package milkman.plugin.grpc.domain;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import milkman.domain.RequestContainer;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GrpcRequestContainer extends RequestContainer {

	private String endpoint;
	
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
