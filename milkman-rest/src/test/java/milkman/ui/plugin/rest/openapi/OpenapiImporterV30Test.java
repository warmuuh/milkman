package milkman.ui.plugin.rest.openapi;

import lombok.SneakyThrows;
import milkman.domain.Collection;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.openapi.OpenapiImporterV30.NewEnvironemtKey;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenapiImporterV30Test {


    @Test
    @SneakyThrows
    void shouldReadSpec(){
        OpenapiImporterV30 importer = new OpenapiImporterV30();
        String yml = IOUtils.toString(getClass().getResourceAsStream("/openapi-example-v3.yml"));

        Collection collection = importer.importCollection(yml, Collections.emptyList()).getLeft();
        assertThat(collection.getName()).isEqualTo("Simple API overview");

        RestRequestContainer req1 = (RestRequestContainer) collection.getRequests().get(0);
        assertThat(req1.getName()).isEqualTo("listVersionsv2");
        assertThat(req1.getHttpMethod()).isEqualTo("GET");
        assertThat(req1.getUrl()).isEqualTo("{{simple-api-overview.server0}}/");
    }


    @Test
    @SneakyThrows
    void shouldReadSpecsServerObjects(){
        OpenapiImporterV30 importer = new OpenapiImporterV30();
        String yml = IOUtils.toString(getClass().getResourceAsStream("/openapi-example-v3.yml"));

        List<NewEnvironemtKey> newEnvKeys = importer.importCollection(yml, Collections.emptyList()).getRight();
        assertThat(newEnvKeys).hasSize(2);
        assertThat(newEnvKeys.get(0).getKeyName()).isEqualTo("simple-api-overview.server0");
        assertThat(newEnvKeys.get(0).getKeyValue()).isEqualTo("http://petstore.swagger.io/api");
        assertThat(newEnvKeys.get(1).getKeyName()).isEqualTo("simple-api-overview.test-server");
        assertThat(newEnvKeys.get(1).getKeyValue()).isEqualTo("http://petstore2.swagger.io/api");
    }

}