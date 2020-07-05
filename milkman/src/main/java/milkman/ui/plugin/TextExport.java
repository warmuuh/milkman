package milkman.ui.plugin;

import milkman.domain.RequestContainer;

public interface TextExport<T extends RequestContainer> {

    public String export(boolean isWindows, T container, Templater templater);
}
