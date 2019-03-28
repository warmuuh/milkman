package milkman.ui.plugin.rest;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import lombok.SneakyThrows;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;

public class RequestProcessor {
	
    CloseableHttpClient httpclient = HttpClients.createDefault();

    
    
    @SneakyThrows
	public RestResponseContainer executeRequest(RestRequestContainer request) {
		RequestBuilder builder = toHttpRequest(request);
		HttpResponse httpResponse = executeRequest(builder);
		RestResponseContainer response = toResponseContainer(httpResponse);
		return response; 
	}



	private HttpResponse executeRequest(RequestBuilder builder) throws IOException, ClientProtocolException {
		HttpUriRequest httpRequest = builder.build();
		HttpResponse httpResponse = httpclient.execute(httpRequest);
		return httpResponse;
	}



	private RequestBuilder toHttpRequest(RestRequestContainer request) {
		RequestBuilder builder = RequestBuilder.create(request.getHttpMethod());
		builder.setUri(request.getUrl());
		return builder;
	}



	private RestResponseContainer toResponseContainer(HttpResponse httpResponse) throws IOException {
		RestResponseContainer response = new RestResponseContainer();
		String body = IOUtils.toString(httpResponse.getEntity().getContent());
		response.getAspects().add(new RestResponseBodyAspect(body));
		return response;
	}
	
}
