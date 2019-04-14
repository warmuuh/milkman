package milkman.domain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.Nulls;

import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.RequestAspect.UnknownRequestAspect;

@Data
@NoArgsConstructor
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS)
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
		return Collections.unmodifiableList(aspects);
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
		aspects.removeIf(a -> a instanceof UnknownRequestAspect);
		for (RequestAspect aspect : aspects) {
			if (aspect.isDirty() && !this.isDirty())
				setDirty(true);
			aspect.propagateDirtyStateTo(this);
		}
	}



	@Override
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		//propagate to children:
		for (RequestAspect aspect : aspects) {
			aspect.setDirty(false, false);
		}
	}
	
	
	
	
	
	
	
}
