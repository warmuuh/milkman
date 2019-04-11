package milkman.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Environment {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EnvironmentEntry {
		String name;
		String value;
		boolean enabled;
	}
	
	String name;
	boolean active;
	boolean global;
	List<EnvironmentEntry> entries = new LinkedList<Environment.EnvironmentEntry>();
	
	public Environment(String name) {
		super();
		this.name = name;
	}

	
	public void setOrAdd(String name, String value) {
		Optional<EnvironmentEntry> maybeEntry = entries.stream().filter(e -> e.name.equals(name)).findAny();
		if (maybeEntry.isPresent()) {
			maybeEntry.get().setValue(value);
		} else {
			EnvironmentEntry entry = new EnvironmentEntry(name, value, true);
			entries.add(entry);
		}
		
	}
	
}
