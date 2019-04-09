package milkman.ui.plugin.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.RequestContent;

import javafx.application.Platform;
import lombok.SneakyThrows;
import milkman.domain.RequestAspect;
import milkman.ui.main.dialogs.CredentialsInputDialog;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestRequestAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

public class RequestProcessor {

	public RequestProcessor() {
		
	}
	
	
	private static UsernamePasswordCredentials proxyCredentials = null;

	@SneakyThrows
	private CloseableHttpClient buildClient() {
		HttpClientBuilder builder = HttpClients.custom()
				.addInterceptorFirst(new RequestContent());
		
		
		if (StringUtils.isNotBlank(HttpOptionsPluginProvider.options().getProxyUrl())) {
			URL url = new URL(HttpOptionsPluginProvider.options().getProxyUrl());
			
			if (proxyCredentials != null) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials( new AuthScope(url.getHost(), url.getPort()), proxyCredentials);			
				builder = builder.setDefaultCredentialsProvider(credsProvider);
			}
			
			builder = builder.setProxy(new HttpHost(url.getHost(), url.getPort()));
		}
		
		return builder
				.build();
	}

	@SneakyThrows
	public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater) {
		HttpUriRequest httpRequest = toHttpRequest(request, templater);
		HttpResponse httpResponse = executeRequest(httpRequest);
		
		AtomicReference<HttpResponse> responseHolder = new AtomicReference<HttpResponse>(httpResponse);
		
		if (httpResponse.getStatusLine().getStatusCode() == 407 
				&& HttpOptionsPluginProvider.options().isAskForProxyAuth()) {
			CountDownLatch latch = new CountDownLatch(1);
			Platform.runLater(() -> {
				CredentialsInputDialog dialog = new CredentialsInputDialog();
				dialog.showAndWait("Proxy Authentication");
				if (!dialog.isCancelled()) {
					proxyCredentials = new UsernamePasswordCredentials( dialog.getUsername(), dialog.getPassword());
					try {
						responseHolder.set(executeRequest(httpRequest));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				latch.countDown();
			});
			latch.await();
		}
		
		RestResponseContainer response = toResponseContainer(responseHolder.get());
		return response;
	}

	private HttpResponse executeRequest(HttpUriRequest httpRequest) throws IOException, ClientProtocolException {
		CloseableHttpClient httpclient = buildClient();
		HttpResponse httpResponse = httpclient.execute(httpRequest);
		return httpResponse;
	}

	@SneakyThrows
	private HttpUriRequest toHttpRequest(RestRequestContainer request, Templater templater) {
		RequestBuilder builder = RequestBuilder.create(request.getHttpMethod());
		builder.setUri(escapeUrl(request, templater));
		builder.setHeader("user-agent", "milkman");

		for (RequestAspect aspect : request.getAspects()) {
			if (aspect instanceof RestRequestAspect) {
				((RestRequestAspect) aspect).enrichRequest(builder, templater);
			}
		}
		
		

		return builder.build();
	}

	private String escapeUrl(RestRequestContainer request, Templater templater)
			throws MalformedURLException, URISyntaxException {
		URL url = new URL(templater.replaceTags(request.getUrl()));
	    URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	    return uri.toASCIIString();
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
