package milkman.plugin.privatebin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.RequestContainer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDataContainer extends AbstractDataContainer {
	private RequestContainer request;
}
