package milkman.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor
@Data
@Slf4j
public class KeySet {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS, visible = true)
	public abstract static class KeyEntry {
		String id;
		String name;
		public abstract String getType();
		public abstract String getValue();

		public String getPreview() {
			return getValue();
		}
	}

	String id;
	String name;
	List<KeyEntry> entries = new LinkedList<>();

	public KeySet(String name) {
		this.id  = UUID.randomUUID().toString();
		this.name = name;
	}

	public Optional<String> getValueForKey(String keyname) {
		return entries.stream()
				.filter(keyEntry -> keyEntry.getName().equals(keyname))
				.map(KeyEntry::getValue)
				.findAny();
	}
}
