package milkman.ui.plugin;

import java.io.IOException;

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

import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.domain.RequestAspect;
import milkman.domain.RequestAspect.CustomUnknownDeserializer;
import milkman.domain.RequestAspect.CustomUnknownSerializer;
import milkman.domain.RequestAspect.UnknownRequestAspect;

@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS)
public interface OptionsObject {

	
	
	// used as deserialization target of unknown Aspects (e.g. removed plugins)
	@Data
	@AllArgsConstructor
	@JsonDeserialize(using = CustomUnknownDeserializer.class)
	@JsonSerialize(using = CustomUnknownSerializer.class)
	public static class UnknownOptionsObject implements OptionsObject {
		private TreeNode content;
	};
	
	
	public static class CustomUnknownDeserializer extends JsonDeserializer<UnknownOptionsObject> {

		@Override
		public UnknownOptionsObject deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			TreeNode tree = p.getCodec().readTree(p);
			
			return new UnknownOptionsObject(tree);
		}
		
	}
	
	public static class CustomUnknownSerializer extends JsonSerializer<UnknownOptionsObject> {

		@Override
		public void serialize(UnknownOptionsObject value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			gen.writeTree(value.content);
		}

		@Override
		public void serializeWithType(UnknownOptionsObject value, JsonGenerator gen, SerializerProvider serializers,
				TypeSerializer typeSer) throws IOException {
			gen.writeTree(value.content);
		}
	}
	
}
