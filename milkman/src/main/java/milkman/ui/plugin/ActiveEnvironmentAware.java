package milkman.ui.plugin;

import java.util.Optional;

import milkman.domain.Environment;

/**
 * 
 * @author pmucha
 *
 */
public interface ActiveEnvironmentAware {

	public void setActiveEnvironment(Optional<Environment> activeEnvironment);
}
