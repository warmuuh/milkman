package milkman.plugin.cassandra.domain;

import lombok.*;
import milkman.domain.RequestContainer;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CassandraRequestContainer extends RequestContainer {

	private String cassandraUrl;
	
	
	
	@Override
	public String getType() {
		return "CQL";
	}

	@Override
	public RequestTypeDescriptor getTypeDescriptor() {
		return new RequestTypeDescriptor("CQL", getClass().getResource("/icons/cassandra.png"));
	}

	public CassandraRequestContainer(String name, String cassandraUrl) {
		super(name);
		this.cassandraUrl = cassandraUrl;
	}
	
}
