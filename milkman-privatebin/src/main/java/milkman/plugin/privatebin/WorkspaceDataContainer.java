package milkman.plugin.privatebin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.Workspace;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDataContainer extends AbstractDataContainer {
	private Workspace workspace;
}
