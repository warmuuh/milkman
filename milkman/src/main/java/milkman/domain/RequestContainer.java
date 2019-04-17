package milkman.domain;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
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
import com.fasterxml.jackson.annotation.Nulls;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS, visible = true)
public abstract class RequestContainer extends Dirtyable implements Searchable {
	private String id = "";
	private boolean inStorage = false;
	private String name;
	private List<RequestAspect> aspects = new LinkedList<RequestAspect>();

	
	public RequestContainer(String name) {
		super();
		this.name = name;
	}
	
	@JsonIgnore
	public abstract String getType();
	
	public void addAspect(RequestAspect aspect) {
		aspect.propagateDirtyStateTo(this);
		aspects.add(aspect);
	}
	
	public List<RequestAspect> getAspects(){
		return aspects;
	}

	public <T extends RequestAspect> Optional<T> getAspect(Class<T> aspectType) {
		return aspects.stream()
				.filter(aspectType::isInstance)
				.findAny()
				.map(a -> (T)a);
	}
	
	@Override
	public boolean match(String searchString) {
		return StringUtils.containsIgnoreCase(name, searchString)
				|| aspects.stream().anyMatch(a -> a.match(searchString));
	}



	public void setAspects(List<RequestAspect> aspects) {
		this.aspects = aspects;
		if (aspects == null) {
			this.aspects = new LinkedList<RequestAspect>();
			return;
		}
			
//		aspects.removeIf(a -> a instanceof UnknownRequestAspect);
		for (RequestAspect aspect : aspects) {
			if (aspect.isDirty() && !this.isDirty())
				setDirty(true);
			aspect.propagateDirtyStateTo(this);
		}
	}



	@Override
	public void setDirty(Boolean dirty) {
		if (dirty == null)
			dirty = false;
		
		super.setDirty(dirty);
		//propagate to children:
		for (RequestAspect aspect : aspects) {
			aspect.setDirty(false, false);
		}
	}
	
	
	
	
	// used as deserialization target of unknown request containers (e.g. removed plugins)
		@Data
		@AllArgsConstructor
		@JsonDeserialize(using = CustomUnknownDeserializer.class)
		@JsonSerialize(using = CustomUnknownSerializer.class)
		public static class UnknownRequestContainer extends RequestContainer {
			private TreeNode content;
			
			@Override
			public String getType() {
				return "UNKNOWN";
			}

			@Override
			public String getName() {
				return "missing plugin";
			}
		};
		
		
		public static class CustomUnknownDeserializer extends JsonDeserializer<UnknownRequestContainer> {

			@Override
			public UnknownRequestContainer deserialize(JsonParser p, DeserializationContext ctxt)
					throws IOException, JsonProcessingException {
				TreeNode tree = p.getCodec().readTree(p);
				
				return new UnknownRequestContainer(tree);
			}
			
		}
		
		public static class CustomUnknownSerializer extends JsonSerializer<UnknownRequestContainer> {

			@Override
			public void serialize(UnknownRequestContainer value, JsonGenerator gen, SerializerProvider serializers)
					throws IOException {
				gen.writeTree(value.content);
			}

			@Override
			public void serializeWithType(UnknownRequestContainer value, JsonGenerator gen, SerializerProvider serializers,
					TypeSerializer typeSer) throws IOException {
				gen.writeTree(value.content);
			}
		}
	
	
}
