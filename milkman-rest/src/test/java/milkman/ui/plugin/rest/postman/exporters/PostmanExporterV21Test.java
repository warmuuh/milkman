package milkman.ui.plugin.rest.postman.exporters;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import lombok.val;
import milkman.domain.Collection;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

class PostmanExporterV21Test {

	@Test
	void test() {
		val sut = new PostmanExporterV21();
		RestRequestContainer request = new RestRequestContainer("testReq", "https://jsonplaceholder.typicode.com/posts", "POST");
		RestBodyAspect bodyAspect = new RestBodyAspect();
		bodyAspect.setBody("{\r\n" + 
				"      \"title\": \"foo\",\r\n" + 
				"      \"body\": \"bar\",\r\n" + 
				"      \"userId\": 1\r\n" + 
				"    }");
		request.addAspect(bodyAspect);
		
		RestHeaderAspect headerAspect = new RestHeaderAspect();
		HeaderEntry headerEntry = new HeaderEntry();
		headerEntry.setId(UUID.randomUUID().toString());
		headerEntry.setName("Content-type");
		headerEntry.setValue("application/json");
		headerAspect.setEntries(Arrays.asList(headerEntry));
		request.addAspect(headerAspect);
		String exportedData = sut.export(new Collection(UUID.randomUUID().toString(), "test", false, Arrays.asList(request), new LinkedList<>()));
		System.out.println(exportedData);
	}

}
