package milkman.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Data;

@Data
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS)
public abstract class SyncDetails {

	private final boolean syncActive = true;
	
	
}
