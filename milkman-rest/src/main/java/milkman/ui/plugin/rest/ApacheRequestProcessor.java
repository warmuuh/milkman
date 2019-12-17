//package milkman.ui.plugin.rest;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URL;
//import java.security.KeyManagementException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.concurrent.atomic.AtomicReference;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.http.Header;
//import org.apache.http.HttpResponse;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.client.methods.RequestBuilder;
//import org.apache.http.conn.ssl.NoopHostnameVerifier;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.protocol.RequestContent;
//import org.apache.http.ssl.SSLContextBuilder;
//import org.apache.http.ssl.TrustStrategy;
//
//import javafx.application.Platform;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import milkman.domain.RequestAspect;
//import milkman.ui.main.dialogs.CredentialsInputDialog;
//import milkman.ui.plugin.Templater;
//import milkman.ui.plugin.rest.domain.HeaderEntry;
//import milkman.ui.plugin.rest.domain.RestRequestAspect;
//import milkman.ui.plugin.rest.domain.RestRequestContainer;
//import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
//import milkman.ui.plugin.rest.domain.RestResponseContainer;
//import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
//
//public class ApacheRequestProcessor implements RequestProcessor {
//
//	private static final String USER_AGENT_HEADER = "User-Agent";
//
//	public ApacheRequestProcessor() {
//		
//	}
//	
//	
//	private static UsernamePasswordCredentials proxyCredentials = null;
//
//	@SneakyThrows
//	private CloseableHttpClient buildClient() {
//		HttpClientBuilder builder = HttpClients.custom()
//				.addInterceptorFirst(new RequestContent());
//		
//		if (HttpOptionsPluginProvider.options().isUseProxy()) {
//			URL url = new URL(HttpOptionsPluginProvider.options().getProxyUrl());
//			
//			if (proxyCredentials != null) {
//				CredentialsProvider credsProvider = new BasicCredentialsProvider();
//				credsProvider.setCredentials( new AuthScope(url.getHost(), url.getPort()), proxyCredentials);			
//				builder = builder.setDefaultCredentialsProvider(credsProvider);
//			}
//			
//			builder.setRoutePlanner(new ProxyExclusionRoutePlanner(url, HttpOptionsPluginProvider.options().getProxyExclusion()).apache());
//		}
//		
//		if (!HttpOptionsPluginProvider.options().isCertificateValidation()) {
//			disableSsl(builder);
//		}
//		
//		if (!HttpOptionsPluginProvider.options().isFollowRedirects()) {
//			builder.disableRedirectHandling();
//		}
//		
//		return builder
//				.build();
//	}
//
//	private void disableSsl(HttpClientBuilder builder)
//			throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
//		builder.setSSLContext(SSLContextBuilder.create().loadTrustMaterial(new TrustStrategy() {
//            @Override
//            public boolean isTrusted(X509Certificate[] chain,
//                String authType) throws CertificateException {
//                return true;
//            }
//        }).build());
//		
//		builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
//	}
//	@Override
//	@SneakyThrows
//	public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater) {
//		HttpUriRequest httpRequest = toHttpRequest(request, templater);
//		AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
//		HttpResponse httpResponse = executeRequest(httpRequest);
//		
//		AtomicReference<HttpResponse> responseHolder = new AtomicReference<HttpResponse>(httpResponse);
//		
//		if (httpResponse.getStatusLine().getStatusCode() == 407 
//				&& HttpOptionsPluginProvider.options().isAskForProxyAuth()) {
//			CountDownLatch latch = new CountDownLatch(1);
//			Platform.runLater(() -> {
//				CredentialsInputDialog dialog = new CredentialsInputDialog();
//				dialog.showAndWait("Proxy Authentication");
//				if (!dialog.isCancelled()) {
//					proxyCredentials = new UsernamePasswordCredentials( dialog.getUsername(), dialog.getPassword());
//					try {
//						startTime.set(System.currentTimeMillis());
//						responseHolder.set(executeRequest(httpRequest));
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				latch.countDown();
//			});
//			latch.await();
//		}
//		
//		var responseTimeInMs = System.currentTimeMillis() - startTime.get();
//		RestResponseContainer response = toResponseContainer(httpRequest.getURI().toString(), responseHolder.get(), responseTimeInMs);
//		return response;
//	}
//
//	private HttpResponse executeRequest(HttpUriRequest httpRequest) throws IOException, ClientProtocolException {
//		CloseableHttpClient httpclient = buildClient();
//		HttpResponse httpResponse = httpclient.execute(httpRequest);
//		return httpResponse;
//	}
//
//	@SneakyThrows
//	private HttpUriRequest toHttpRequest(RestRequestContainer request, Templater templater) {
//		RequestBuilder builder = RequestBuilder.create(request.getHttpMethod());
//		builder.setUri(HttpUtil.escapeUrl(request, templater));
//		
//
//		for (RequestAspect aspect : request.getAspects()) {
//			if (aspect instanceof RestRequestAspect) {
//				((RestRequestAspect) aspect).enrichRequest(new ApacheRequestBuilder(builder), templater);
//			}
//		}
//		
//		if (builder.getMethod().equals("GET")) {
//			builder.setEntity(null);
//		}
//		
//		if (builder.getFirstHeader(USER_AGENT_HEADER) == null)
//			builder.setHeader(USER_AGENT_HEADER, "Milkman");
//
//		return builder.build();
//	}
//
//
//	private RestResponseContainer toResponseContainer(String url, HttpResponse httpResponse, long responseTimeInMs) throws IOException {
//		RestResponseContainer response = new RestResponseContainer(url);
//		String body = "";
//		if (httpResponse.getEntity() != null && httpResponse.getEntity().getContent() != null)
//			body = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
//		response.getAspects().add(new RestResponseBodyAspect(body));
//		
//		RestResponseHeaderAspect headers = new RestResponseHeaderAspect();
//		for (Header h : httpResponse.getAllHeaders()) {
//			headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), h.getName(), h.getValue(), false));
//		}
//		response.getAspects().add(headers);
//		
//		buildStatusView(httpResponse, response, responseTimeInMs);
//		
//		return response;
//	}
//
//	private void buildStatusView(HttpResponse httpResponse, RestResponseContainer response, long responseTimeInMs) {
//		var status = httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase();
//		response.getStatusInformations().complete(Map.of(
//				"Status", status, 
//				"Time", responseTimeInMs + "ms"));
//	}
//	
//	@RequiredArgsConstructor
//	private class ApacheRequestBuilder implements HttpRequestBuilder{
//		private final RequestBuilder builder;
//
//		@Override
//		public void addHeader(String key, String value) {
//			builder.addHeader(key, value);
//		}
//
//		@Override
//		public void setBody(String body) {
//			try {
//				builder.setEntity(new StringEntity(body));
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//}
