package milkman.ui.plugin.rest;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestAspect;
import milkman.ui.main.dialogs.CredentialsInputDialog;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestRequestAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;

@Slf4j
public class JavaRequestProcessor implements RequestProcessor {


	static {
		//this enables using "Basic" authentication for https-requests over http proxy (disabled by default)
		//see https://bugs.openjdk.java.net/browse/JDK-8229962 for more details
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
	}
	
	private static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
	private static final String USER_AGENT_HEADER = "User-Agent";

	
	private static PasswordAuthentication proxyCredentials = null;

	@SneakyThrows
	private HttpClient buildClient() {
		Builder builder = HttpClient.newBuilder();
		
		if (HttpOptionsPluginProvider.options().isUseProxy()) {
			URL url = new URL(HttpOptionsPluginProvider.options().getProxyUrl());
			builder.proxy(new ProxyExclusionRoutePlanner(url, HttpOptionsPluginProvider.options().getProxyExclusion()).java());
			
			//we dont use Authenticator because it might result in an exception if there is a 401 response
			// see https://github.com/AdoptOpenJDK/openjdk-jdk11/blob/master/src/java.net.http/share/classes/jdk/internal/net/http/AuthenticationFilter.java#L263
			//we manually add Proxy-Authorization header instead
//			if (proxyCredentials != null) {
//				builder.authenticator(new Authenticator() {
//					@Override
//					protected PasswordAuthentication getPasswordAuthentication() {
//						return proxyCredentials;
//					}
//				});
//			}
		}
		
		if (!HttpOptionsPluginProvider.options().isCertificateValidation()) {
			disableSsl(builder);
		}
		
		if (HttpOptionsPluginProvider.options().isFollowRedirects()) {
			builder.followRedirects(Redirect.ALWAYS);
		}
		
		return builder
				.build();
	}

	private void disableSsl(Builder builder)
			throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        public void checkClientTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    }
		};

		// Install the all-trusting trust manager
		try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    builder.sslContext(sc);
		} catch (Exception e) {
		}

	}
	@Override
	@SneakyThrows
	public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater) {
		HttpRequest httpRequest = toHttpRequest(request, templater);
		AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
		HttpResponse<String> httpResponse = executeRequest(httpRequest);
		
		AtomicReference<HttpResponse<String>> responseHolder = new AtomicReference<HttpResponse<String>>(httpResponse);
		
		if (httpResponse.statusCode() == 407 
				&& HttpOptionsPluginProvider.options().isAskForProxyAuth()) {
			CountDownLatch latch = new CountDownLatch(1);
			Platform.runLater(() -> {
				CredentialsInputDialog dialog = new CredentialsInputDialog();
				dialog.showAndWait("Proxy Authentication");
				if (!dialog.isCancelled()) {
					proxyCredentials = new PasswordAuthentication(dialog.getUsername(), dialog.getPassword().toCharArray());
					try {
						var newRequest = toHttpRequest(request, templater);
						startTime.set(System.currentTimeMillis());
						responseHolder.set(executeRequest(newRequest));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				latch.countDown();
			});
			latch.await();
		}
		
		var responseTimeInMs = System.currentTimeMillis() - startTime.get();
		RestResponseContainer response = toResponseContainer(httpRequest.uri().toString(), responseHolder.get(), responseTimeInMs);
		return response;
	}

	private HttpResponse<String> executeRequest(HttpRequest httpRequest) throws IOException {
		HttpClient httpclient = buildClient();
		HttpResponse<String> httpResponse;
		try {
			httpResponse = httpclient.send(httpRequest, BodyHandlers.ofString());
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		return httpResponse;
	}

	@SneakyThrows
	private HttpRequest toHttpRequest(RestRequestContainer request, Templater templater) {
		HttpRequest.Builder builder = HttpRequest.newBuilder();
		builder.uri(new URI(HttpUtil.escapeUrl(request, templater)));

		for (RequestAspect aspect : request.getAspects()) {
			if (aspect instanceof RestRequestAspect) {
				((RestRequestAspect) aspect).enrichRequest(new JavaRequestBuilder(builder, request.getHttpMethod()), templater);
			}
		}
		
		if (builder.build().headers().firstValue(USER_AGENT_HEADER).isEmpty())
			builder.setHeader(USER_AGENT_HEADER, "Milkman");

		if (proxyCredentials != null) {
			builder.setHeader(PROXY_AUTHORIZATION_HEADER, HttpUtil.authorizationHeaderValue(proxyCredentials));
		}
		
		return builder.build();
	}

	

	private RestResponseContainer toResponseContainer(String url, HttpResponse<String> httpResponse, long responseTimeInMs) throws IOException {
		RestResponseContainer response = new RestResponseContainer(url);
		String body = httpResponse.body();
		if (body == null)
			body = "";
		response.getAspects().add(new RestResponseBodyAspect(body));
		
		RestResponseHeaderAspect headers = new RestResponseHeaderAspect();
		for (Entry<String, List<String>> h : httpResponse.headers().map().entrySet()) {
			for(String hval : h.getValue()) {
				headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), h.getKey(), hval, false));
			}
		}
		response.getAspects().add(headers);
		
		buildStatusView(httpResponse, response, responseTimeInMs);
		
		return response;
	}

	private void buildStatusView(HttpResponse<String> httpResponse, RestResponseContainer response, long responseTimeInMs) {
		response.getStatusInformations().complete(Map.of(
				"Status", ""+httpResponse.statusCode(), 
				"Time", responseTimeInMs + "ms"));
	}

	@RequiredArgsConstructor
	private class JavaRequestBuilder implements HttpRequestBuilder{
		private final HttpRequest.Builder builder;
		private final String method;
		
		@Override
		public void addHeader(String key, String value) {
			builder.header(key, value);
		}

		@Override
		public void setBody(String body) {
			if (method.equals("GET") || method.equals("DELETE")) {
				builder.method(method, BodyPublishers.noBody());
			} else {
				builder.method(method, BodyPublishers.ofString(body));
			}
		}
	}
	
}
