package milkman.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
@Slf4j
public class KeySet {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public abstract static class KeyEntry {
		String id;
		String name;
		public abstract String getValue();
	}

	String id;
	String name;
	List<KeyEntry> entries = new LinkedList<>();

	public KeySet(String name) {
		this.id  = UUID.randomUUID().toString();
		this.name = name;
	}
}
