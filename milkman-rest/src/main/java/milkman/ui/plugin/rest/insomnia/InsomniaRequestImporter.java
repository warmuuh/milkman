package milkman.ui.plugin.rest.insomnia;

import java.util.List;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaFile;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaResource;

public interface InsomniaRequestImporter {

  boolean supportRequestType(InsomniaResource resource);

  RequestContainer convert(InsomniaResource resource, List<InsomniaFile> files);

}
