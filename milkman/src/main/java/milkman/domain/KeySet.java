package milkman.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import milkman.ui.plugin.OptionsObject;
import milkman.ui.plugin.OptionsObject.CustomUnknownDeserializer;
import milkman.ui.plugin.OptionsObject.CustomUnknownSerializer;
import milkman.ui.plugin.OptionsObject.UnknownOptionsObject;

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

		// used as deserialization target of unknown Aspects (e.g. removed plugins)
		@Data
		@AllArgsConstructor
		@JsonDeserialize(using = CustomUnknownDeserializer.class)
		@JsonSerialize(using = CustomUnknownSerializer.class)
		public static class UnknownKeyEntryObject extends KeyEntry {
			private TreeNode content;

			@Override
			public String getId() {
				return content.get("id").toString();
			}

			@Override
			public String getName() {
				return content.get("name").toString();
			}

			@Override
			public String getType() {
				return "UNKNOWN";
			}

			@Override
			public String getValue() {
				return "UNKNOWN";
			}
		};


		public static class CustomUnknownDeserializer extends JsonDeserializer<UnknownKeyEntryObject> {

			@Override
			public UnknownKeyEntryObject deserialize(JsonParser p, DeserializationContext ctxt)
					throws IOException, JsonProcessingException {
				TreeNode tree = p.getCodec().readTree(p);

				return new UnknownKeyEntryObject(tree);
			}

		}

		public static class CustomUnknownSerializer extends JsonSerializer<UnknownKeyEntryObject> {

			@Override
			public void serialize(UnknownKeyEntryObject value, JsonGenerator gen, SerializerProvider serializers)
					throws IOException {
				gen.writeTree(value.content);
			}

			@Override
			public void serializeWithType(UnknownKeyEntryObject value, JsonGenerator gen, SerializerProvider serializers,
					TypeSerializer typeSer) throws IOException {
				gen.writeTree(value.content);
			}
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
