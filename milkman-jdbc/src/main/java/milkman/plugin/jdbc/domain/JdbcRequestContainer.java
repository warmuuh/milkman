package milkman.plugin.jdbc.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.RequestContainer;

@Data
@NoArgsConstructor
public class JdbcRequestContainer extends RequestContainer {

	private String jdbcUrl;
	
	
	
	@Override
	public String getType() {
		return "SQL";
	}



	public JdbcRequestContainer(String name, String jdbcUrl) {
		super(name);
		this.jdbcUrl = jdbcUrl;
	}
	
}
