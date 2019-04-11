package milkman.plugin.explore;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class JqlQueryAspect extends RequestAspect {
	private String query = "";
}
