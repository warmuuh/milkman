package milkman.ui.plugin.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.protocol.RequestContent;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

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
		
		
		if (HttpOptionsPluginProvider.options().isUseProxy()) {
			URL url = new URL(HttpOptionsPluginProvider.options().getProxyUrl());
			
			if (proxyCredentials != null) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials( new AuthScope(url.getHost(), url.getPort()), proxyCredentials);			
				builder = builder.setDefaultCredentialsProvider(credsProvider);
			}
			
			HttpHost proxyHost = new HttpHost(url.getHost(), url.getPort());
			builder.setRoutePlanner(new ProxyExclusionRoutePlanner(proxyHost, HttpOptionsPluginProvider.options().getProxyExclusion()));
		}
		
		if (!HttpOptionsPluginProvider.options().isCertificateValidation()) {
			disableSsl(builder);
		}
		
		return builder
				.build();
	}

	private void disableSsl(HttpClientBuilder builder)
			throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		builder.setSSLContext(SSLContextBuilder.create().loadTrustMaterial(new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain,
                String authType) throws CertificateException {
                return true;
            }
        }).build());
		
		builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
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
		String finalUrl = templater.replaceTags(request.getUrl());
		URL url = new URL(URLDecoder.decode(finalUrl));
	    URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	    return uri.toASCIIString();
	}

	private RestResponseContainer toResponseContainer(HttpResponse httpResponse) throws IOException {
		RestResponseContainer response = new RestResponseContainer();
		String body = "";
		if (httpResponse.getEntity() != null && httpResponse.getEntity().getContent() != null)
			body = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
		response.getAspects().add(new RestResponseBodyAspect(body));
		
		RestResponseHeaderAspect headers = new RestResponseHeaderAspect();
		for (Header h : httpResponse.getAllHeaders()) {
			headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), h.getName(), h.getValue(), false));
		}
		response.getAspects().add(headers);
		
		buildStatusView(httpResponse, response);
		
		return response;
	}

	private void buildStatusView(HttpResponse httpResponse, RestResponseContainer response) {
		response.getStatusInformations().put("Status", httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase());
	}

}
