package milkman.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import milkman.utils.Event0;
import milkman.utils.PropertyChangeEvent;

@Data
public class Dirtyable {

	private Boolean dirty = false;
	
	@JsonIgnore
	public final PropertyChangeEvent<Boolean> onDirtyChange = new PropertyChangeEvent<>();
	@JsonIgnore
	public final Event0 onInvalidate = new Event0();
	
	public void setDirty(Boolean dirty) {
		setDirty(Boolean.TRUE.equals(dirty), true);
	}
	public void setDirty(boolean dirty, boolean notification) {
		onInvalidate.invoke();
		if (dirty != this.dirty) {
			if (notification)
				onDirtyChange.invoke(this.dirty, dirty);
			this.dirty = dirty;
		}
	}
	
	public void propagateDirtyStateTo(Dirtyable other) {
		onDirtyChange.add((o,n) -> other.setDirty(n));
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	
}
