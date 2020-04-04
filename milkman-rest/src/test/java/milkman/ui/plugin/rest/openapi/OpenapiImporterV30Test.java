package milkman.ui.plugin.rest.openapi;

import lombok.SneakyThrows;
import milkman.domain.Collection;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenapiImporterV30Test {


    @Test
    @SneakyThrows
    void shouldReadSpec(){
        OpenapiImporterV30 importer = new OpenapiImporterV30();
        String yml = IOUtils.toString(getClass().getResourceAsStream("/openapi-example-v3.yml"));

        Collection collection = importer.importCollection(yml);
        assertThat(collection.getName()).isEqualTo("Simple API overview");

        RestRequestContainer req1 = (RestRequestContainer) collection.getRequests().get(0);
        assertThat(req1.getName()).isEqualTo("listVersionsv2");
        assertThat(req1.getHttpMethod()).isEqualTo("GET");
        assertThat(req1.getUrl()).isEqualTo("http://petstore.swagger.io/api/");
    }

}