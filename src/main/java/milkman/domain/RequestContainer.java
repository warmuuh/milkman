package milkman.domain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS)
public abstract class RequestContainer extends Dirtyable {

	private String id = "";
	private String name;
	private List<RequestAspect> aspects = new LinkedList<RequestAspect>();

	
	public RequestContainer(String name) {
		super();
		this.name = name;
	}
	
	
	
	public void addAspect(RequestAspect aspect) {
		aspect.propagateDirtyStateTo(this);
		aspects.add(aspect);
	}
	
	public List<RequestAspect> getAspects(){
		return Collections.unmodifiableList(aspects);
	}
}
