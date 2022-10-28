package milkman.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.samskivert.mustache.Mustache;
import milkman.domain.RequestAspect;
import milkman.domain.RequestContainer;
import milkman.exporter.CustomTemplateService.CompiledTemplate;
import org.junit.jupiter.api.Test;

class CustomTemplateServiceTest {


  @Test
  void shouldRenderTemplate() {
    CompiledTemplate template = new CompiledTemplate(Mustache.compiler().compile("{{testVar}} {{headers.value}}"), "testtemplate");
    TestRequestContainer requestContainer = new TestRequestContainer();
    requestContainer.addAspect(new RequestAspect("headers") {
      String value= "World";
    });
    String result = template.render(requestContainer);
    assertThat(result).isEqualTo("Hello World");
  }

  private static class TestRequestContainer extends RequestContainer {

    String testVar = "Hello";

    @Override
    public String getType() {
      return "test-type";
    }
  }
}