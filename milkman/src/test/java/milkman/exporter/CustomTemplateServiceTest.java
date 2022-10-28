package milkman.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import com.samskivert.mustache.Mustache;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.exporter.CustomTemplateService.CompiledTemplate;
import org.junit.jupiter.api.Test;

class CustomTemplateServiceTest {


  @Test
  void shouldRenderTemplate() {
    CompiledTemplate template = new CompiledTemplate(
        new CustomTemplateService().compileTemplate("{{testVar}} {{headers.value}}"),
        "testtemplate");
    TestRequestContainer requestContainer = new TestRequestContainer();
    requestContainer.addAspect(new RequestAspect("headers") {
      String value = "World";
    });
    String result = template.render(requestContainer);
    assertThat(result).isEqualTo("Hello World");
  }


  @Test
  void shouldRenderTemplateWithWhitespaceControl() {
    CompiledTemplate template = new CompiledTemplate(
        new CustomTemplateService().compileTemplate("  {{- testVar -}} \r\ncruel \n\t{{-headers.value-}}  \n  "),
        "testtemplate");
    TestRequestContainer requestContainer = new TestRequestContainer();
    requestContainer.addAspect(new RequestAspect("headers") {
      String value = "World";
    });
    String result = template.render(requestContainer);
    assertThat(result).isEqualTo("HellocruelWorld");
  }

  private static class TestRequestContainer extends RequestContainer {

    String testVar = "Hello";

    @Override
    public String getType() {
      return "test-type";
    }
  }
}