package milkman.plugin.note;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class NotesAspect extends RequestAspect {
	private String note = "";
}
