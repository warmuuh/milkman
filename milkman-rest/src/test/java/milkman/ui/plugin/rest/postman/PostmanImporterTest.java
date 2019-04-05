package milkman.ui.plugin.rest.postman;


import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import milkman.domain.Collection;

class PostmanImporterTest {

	@Test
	void shouldConvertProperly() throws IOException, Exception {
		PostmanImporter sut = new PostmanImporter();
		Collection collection  = sut.importString(IOUtils.toString(getClass().getResourceAsStream("/test.postman_collection"), "UTF-8"));
		assertThat(collection.getName()).isEqualTo("test");
	}

}
