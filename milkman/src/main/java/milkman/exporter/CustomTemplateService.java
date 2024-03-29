package milkman.exporter;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class CustomTemplateService {

  List<CompiledTemplate> getTemplatesForType(String type) {
    return CustomTemplateOptionsProvider.options().getExportTemplates().stream()
        .filter(t -> t.getRequestType().equals(type))
        .map(t -> new CompiledTemplate(compileTemplate(t.getTemplate()), t.getName()))
        .collect(Collectors.toList());
  }

  List<CompiledTemplate> getPredefinedTemplates(String type) {
    return loadPredefinedTemplates().stream()
        .filter(t -> t.getRequestType().equals(type))
        .map(t -> new CompiledTemplate(compileTemplate(t.getTemplate()), t.getName()))
        .collect(Collectors.toList());
  }


  List<ExportTemplate> loadPredefinedTemplates() {
    return getClass().getClassLoader().resources("META-INF/templates.properties")
        .map(url -> {
          Properties properties = new Properties();
          try {
            properties.load(url.openStream());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          return properties;
        }).flatMap(properties -> loadAllTemplates(properties))
        .collect(Collectors.toList());
  }

  private Stream<ExportTemplate> loadAllTemplates(Properties properties) {
    return Arrays.stream(properties.getProperty("templates").split(","))
        .map(String::trim)
        .map(templateId -> {
          String templatePath = "META-INF/" + templateId + ".template";
          InputStream templateResource = getClass().getClassLoader().getResourceAsStream(templatePath);
          try {
            String template = IOUtils.toString(templateResource, Charset.defaultCharset());
            return new ExportTemplate(
                properties.getProperty(templateId + ".name"),
                properties.getProperty(templateId + ".type"),
                template
            );
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

        });
  }


  Template compileTemplate(String template) {
    //apply whitespace control
    String cleanedTemplate = template
        .replaceAll("\\s*\\{\\{-", "{{")
        .replaceAll("-}}\\s*", "}}")
        .replaceAll("\\s*\\{\\{_", " {{")
        .replaceAll("_}}\\s*", "}} ");
    return Mustache.compiler()
        .withEscaper(str -> URLEncoder.encode(str, StandardCharsets.UTF_8))
        .emptyStringIsFalse(true)
        .compile(cleanedTemplate);
  }

  public boolean canHandleRequestType(String type) {
    return Stream.concat(
        CustomTemplateOptionsProvider.options().getExportTemplates().stream(),
        loadPredefinedTemplates().stream())
        .anyMatch(t -> t.getRequestType().equals(type));
  }


  @RequiredArgsConstructor
  public static class CompiledTemplate {

    private final Template template;
    @Getter
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
