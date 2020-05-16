package milkman.ui.plugin.rest.postman.exporters;

import lombok.val;
import milkman.domain.Collection;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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


	@ParameterizedTest
	@MethodSource
	public void shouldParseUrl(String url, String protocol, String host, String port, String path, String query){
		var parsedUrl = new PostmanExporterV21().parseUrl(url);
		assertThat(parsedUrl.getProtocol()).isEqualTo(protocol);
		assertThat(parsedUrl.getHost()).isEqualTo(host);
		assertThat(parsedUrl.getPort()).isEqualTo(port);
		assertThat(parsedUrl.getPath()).isEqualTo(path);
		String actualQryStr = Optional.ofNullable(parsedUrl.getQuery())
				.map(actualQuery -> actualQuery.stream().map(q -> q.getKey() + "="+q.getValue()).collect(Collectors.joining("&")))
				.orElse(null);
		assertThat(actualQryStr).isEqualTo(query);

	}

	private static Stream<Arguments> shouldParseUrl(){
		return Stream.of(
				Arguments.arguments("http://{{host.name}}/path/s/v2//to/test/{{id}}?edited={{boolean}}", "http", "{{host.name}}", null, "/path/s/v2//to/test/{{id}}", "edited={{boolean}}"),
				Arguments.arguments("https://{{host.name}}:80/?test", "https", "{{host.name}}", "80", "/", "test="),
				Arguments.arguments("https://{{host.name}}", "https", "{{host.name}}", null, null, null)
		);
	}

}
