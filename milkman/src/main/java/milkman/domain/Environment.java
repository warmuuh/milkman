package milkman.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Data
@Slf4j
public class Environment {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EnvironmentEntry {
		String id;
		String name;
		String value;
		boolean enabled;
	}
	
	String id;
	String name;
	boolean active;
	boolean global;
	List<EnvironmentEntry> entries = new LinkedList<Environment.EnvironmentEntry>();
	
	public Environment(String name) {
		super();
		this.id  = UUID.randomUUID().toString();
		this.name = name;
	}

	
	public void setOrAdd(String name, String value) {
		if (name == null) {
			log.error("Variable name is null");
			return;
		}
		
		Optional<EnvironmentEntry> maybeEntry = entries.stream().filter(e -> e.name.equals(name)).findAny();
		if (maybeEntry.isPresent()) {
			maybeEntry.get().setValue(value);
		} else {
			EnvironmentEntry entry = new EnvironmentEntry(UUID.randomUUID().toString(), name, value, true);
			entries.add(entry);
		}
	}
	
}
