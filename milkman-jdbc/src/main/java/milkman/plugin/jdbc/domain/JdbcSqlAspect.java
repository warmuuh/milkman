package milkman.plugin.jdbc.domain;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class JdbcSqlAspect extends RequestAspect {

	String sql = "";
	
}
