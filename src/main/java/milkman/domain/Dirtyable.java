package milkman.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import milkman.utils.PropertyChangeEvent;

@Data
public class Dirtyable {

	private boolean dirty;
	
	@JsonIgnore
	public final PropertyChangeEvent<Boolean> onDirtyChange = new PropertyChangeEvent<>();
	
	public void setDirty(boolean dirty) {
		if (dirty != this.dirty) {
			onDirtyChange.invoke(this.dirty, dirty);
			this.dirty = dirty;
		}
	}
	
	public void propagateDirtyStateTo(Dirtyable other) {
		onDirtyChange.add((o,n) -> other.setDirty(n));
	}
}
