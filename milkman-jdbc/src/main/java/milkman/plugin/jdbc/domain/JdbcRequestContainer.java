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


	@Override
	public RequestTypeDescriptor getTypeDescriptor() {
		return new RequestTypeDescriptor("SQL", getClass().getResource("/icons/sql.png"));
	}

	public JdbcRequestContainer(String name, String jdbcUrl) {
		super(name);
		this.jdbcUrl = jdbcUrl;
	}
	
}
