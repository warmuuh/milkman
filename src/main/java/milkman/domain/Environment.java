package milkman.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Environment {

	@Data
	public static class EnvironmentEntry {
		String name;
		String value;
		boolean enabled;
	}
	
	String name;
	boolean active;
	List<EnvironmentEntry> entries = new LinkedList<Environment.EnvironmentEntry>();
	
	public Environment(String name) {
		super();
		this.name = name;
	}

	
}
