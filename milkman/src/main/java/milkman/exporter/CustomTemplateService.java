package milkman.exporter;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;

public class CustomTemplateService {

  List<CompiledTemplate> getTemplatesForType(String type) {
    return CustomTemplateOptionsProvider.options().getExportTemplates().stream()
        .filter(t -> t.getRequestType().equals(type))
        .map(t -> new CompiledTemplate(Mustache.compiler().compile(t.getTemplate()), t.getName()))
        .collect(Collectors.toList());
  }

  public boolean canHandleRequestType(String type) {
    return CustomTemplateOptionsProvider.options().getExportTemplates().stream()
        .anyMatch(t -> t.getRequestType().equals(type));
  }


  @RequiredArgsConstructor
  public static class CompiledTemplate {
    private final Template template;
    private final String templateName;

    public String render(RequestContainer requestContainer) {
      return template.execute(new RequestContainerTemplateContext(requestContainer));
    }

    @Override
    public String toString() {
      return templateName;
    }
  }
}