package milkman.plugin.privatebin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDataContainer extends AbstractDataContainer {
	private Collection collection;
}
