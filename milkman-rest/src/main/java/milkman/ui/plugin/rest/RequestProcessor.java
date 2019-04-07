package milkman.ui.plugin.rest;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.RequestContent;

import javafx.scene.control.Label;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestRequestAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

public class RequestProcessor {

	CloseableHttpClient httpclient;

	public RequestProcessor() {
		httpclient = HttpClients.custom().addInterceptorFirst(new RequestContent()).build();
	}

	@SneakyThrows
	public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater) {
		HttpUriRequest httpRequest = toHttpRequest(request, templater);
		HttpResponse httpResponse = executeRequest(httpRequest);
		RestResponseContainer response = toResponseContainer(httpResponse);
		return response;
	}

	private HttpResponse executeRequest(HttpUriRequest httpRequest) throws IOException, ClientProtocolException {
		HttpResponse httpResponse = httpclient.execute(httpRequest);
		return httpResponse;
	}

	@SneakyThrows
	private HttpUriRequest toHttpRequest(RestRequestContainer request, Templater templater) {
		RequestBuilder builder = RequestBuilder.create(request.getHttpMethod());
		builder.setUri(templater.replaceTags(request.getUrl()));
		builder.setHeader("user-agent", "milkman");

		for (RequestAspect aspect : request.getAspects()) {
			if (aspect instanceof RestRequestAspect) {
				((RestRequestAspect) aspect).enrichRequest(builder, templater);
			}
		}
		
		

		return builder.build();
	}

	private RestResponseContainer toResponseContainer(HttpResponse httpResponse) throws IOException {
		RestResponseContainer response = new RestResponseContainer();
		String body = IOUtils.toString(httpResponse.getEntity().getContent());
		response.getAspects().add(new RestResponseBodyAspect(body));
		
		RestResponseHeaderAspect headers = new RestResponseHeaderAspect();
		for (Header h : httpResponse.getAllHeaders()) {
			headers.getEntries().add(new HeaderEntry(h.getName(), h.getValue(), false));
		}
		response.getAspects().add(headers);
		
		buildStatusView(httpResponse, response);
		
		return response;
	}

	private void buildStatusView(HttpResponse httpResponse, RestResponseContainer response) {
		response.getStatusInformations().put("Status", httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase());
	}

}
