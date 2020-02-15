package milkman.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.Data;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS, visible = true)
public abstract class RequestAspect extends Dirtyable implements Searchable {
	
	private final String name;
	
	public RequestAspect(String name) {
		super();
		this.name = name;
	}

	@Override
	public boolean match(String searchString) {
		return false;
	}

	@JsonIgnore
	public String getName() {
		return name;
	}
	
	
	
	// used as deserialization target of unknown Aspects (e.g. removed plugins)
	@Data
	@JsonDeserialize(using = CustomUnknownDeserializer.class)
	@JsonSerialize(using = CustomUnknownSerializer.class)
	public static class UnknownRequestAspect extends RequestAspect {
		private TreeNode content;
		
		public UnknownRequestAspect(TreeNode content) {
			super("unknown");
			this.content = content;
		}
		
	};
	
	
	public static class CustomUnknownDeserializer extends JsonDeserializer<UnknownRequestAspect> {

		@Override
		public UnknownRequestAspect deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			TreeNode tree = p.getCodec().readTree(p);
			
			return new UnknownRequestAspect(tree);
		}
		
	}
	
	public static class CustomUnknownSerializer extends JsonSerializer<UnknownRequestAspect> {

		@Override
		public void serialize(UnknownRequestAspect value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			gen.writeTree(value.content);
		}

		@Override
		public void serializeWithType(UnknownRequestAspect value, JsonGenerator gen, SerializerProvider serializers,
				TypeSerializer typeSer) throws IOException {
			gen.writeTree(value.content);
		}
	}
	
}
