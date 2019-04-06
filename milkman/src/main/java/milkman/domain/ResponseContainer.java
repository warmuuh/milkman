package milkman.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS)
public abstract class ResponseContainer {

	private List<ResponseAspect> aspects = new LinkedList<ResponseAspect>();

	
	@JsonIgnore
	private Map<String, String> statusInformations = new HashMap<String, String>();
	
	
	public <T extends ResponseAspect> Optional<T> aspect(Class<T> type){
		return aspects.stream().filter(a -> type.isInstance(a)).map(a -> (T)a).findAny();
	}
}
