package milkman.ui.plugin.rest;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.ResponseInfo;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
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
import milkman.utils.AsyncResponseControl.AsyncControl;
import milkman.utils.Event0;
import milkman.utils.reactive.Subscribers;

@Slf4j
public class JavaRequestProcessor implements RequestProcessor {


	static {
		//this enables using "Basic" authentication for https-requests over http proxy (disabled by default)
		//see https://bugs.openjdk.java.net/browse/JDK-8229962 for more details
//		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
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
	public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater, AsyncControl asyncControl) {
		HttpRequest httpRequest = toHttpRequest(request, templater);
		SubmissionPublisher<String> publisher = new SubmissionPublisher<String>();
		Publisher<String> buffer = Subscribers.buffer(publisher);
		asyncControl.triggerReqeuestStarted();
		AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
		StringSubscriber responseSubscriber = executeRequest(httpRequest, publisher, asyncControl.onCancellationRequested);
		
		var responseF = responseSubscriber.getResponseInfo().thenApply(responseInfo -> {
			AtomicReference<StringSubscriber> responseHolder = new AtomicReference<>(responseSubscriber);
			if (responseInfo.statusCode() == 407 
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
							responseHolder.set(executeRequest(newRequest, publisher, asyncControl.onCancellationRequested));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					latch.countDown();
				});
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			return responseHolder.get();
		});
		
		
		responseF
		.thenCompose(StringSubscriber::getBody) // body fulfills only after request is completed
		.handle((res, e) -> {
			if (res != null) {
				asyncControl.triggerRequestSucceeded();
			}
			else 
				asyncControl.triggerRequestFailed(e);	
			return null;
		});
		
		
		RestResponseContainer response = toResponseContainer(httpRequest.uri().toString(),
																publisher,
																responseF.thenCompose(StringSubscriber::getResponseInfo), 
																startTime);
		return response;
	}

	private StringSubscriber executeRequest(HttpRequest httpRequest, SubmissionPublisher<String> publisher, Event0 cancellationEvent) throws IOException {
		HttpClient httpclient = buildClient();
		var stringSubscriber = new StringSubscriber(publisher);
		var future = httpclient.sendAsync(httpRequest, responseInfo -> stringSubscriber.onResponse(responseInfo));
		cancellationEvent.add(stringSubscriber::cancel);
		return stringSubscriber;
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

	

	private RestResponseContainer toResponseContainer(String url, Publisher<String> bodyPublisher, CompletableFuture<ResponseInfo> httpResponse, AtomicLong startTime) throws IOException {
		RestResponseContainer response = new RestResponseContainer(url);
		response.getAspects().add(new RestResponseBodyAspect(bodyPublisher));
		
		var entries = httpResponse.thenApply(res -> {
			List<HeaderEntry> result = new LinkedList<>();
			for (Entry<String, List<String>> h : res.headers().map().entrySet()) {
				for(String hval : h.getValue()) {
					result.add(new HeaderEntry(UUID.randomUUID().toString(), h.getKey(), hval, false));
				}
			}
			return result;
		});
		RestResponseHeaderAspect headers = new RestResponseHeaderAspect(entries);
		
		response.getAspects().add(headers);
		
		httpResponse.thenAccept(res -> {
			var responseTimeInMs = System.currentTimeMillis() - startTime.get();
			buildStatusView(res, response, responseTimeInMs);
		});
		
		return response;
	}

	private void buildStatusView(ResponseInfo httpResponse, RestResponseContainer response, long responseTimeInMs) {
		String versionStr = httpResponse.version().toString();
		if (httpResponse.version() == Version.HTTP_1_1) {
			versionStr = "1.1";
		} else if (httpResponse.version() == Version.HTTP_2) {
			versionStr = "2.0";
		}
		LinkedHashMap<String, String> statusKeys = new LinkedHashMap<String, String>();
		statusKeys.put("Status", ""+httpResponse.statusCode());
		statusKeys.put("Time", responseTimeInMs + "ms");
		statusKeys.put("Http", versionStr);
		
		response.getStatusInformations().complete(statusKeys);
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
