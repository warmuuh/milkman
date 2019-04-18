package milkman.plugin.jdbc.domain;

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
