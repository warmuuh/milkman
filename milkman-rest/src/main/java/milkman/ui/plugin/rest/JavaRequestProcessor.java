package milkman.ui.plugin.rest;

import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.ResponseContainer.StyledText;
import milkman.ui.main.dialogs.CredentialsInputDialog;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.DebugRequestBodyAspect;
import milkman.ui.plugin.rest.domain.DebugRequestHeaderAspect;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import milkman.ui.plugin.rest.http3.JettyHttp3Client;
import milkman.utils.AsyncResponseControl.AsyncControl;
import milkman.utils.json.BlockingFluxByteToStringConverter;
import org.apache.commons.lang3.StringUtils;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

@Slf4j
public class JavaRequestProcessor implements RequestProcessor {


	static {
		//this enables using "Basic" authentication for https-requests over http proxy (disabled by default)
		//see https://bugs.openjdk.java.net/browse/JDK-8229962 for more details
//		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

		// allows to call https endpoints by different names, such as IP
		// although this should be behind the validate-certificate option, this has to be set here already,
		// otherwise, it will not be picked up by httpclient.
		System.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
	}
	
	private static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
	private static final String USER_AGENT_HEADER = "User-Agent";
	private static final String CONTENT_TYPE_HEADER = "Content-Type";

	
	private static PasswordAuthentication proxyCredentials;
	private final Pattern realmPattern = Pattern.compile("realm=(\".*\")");

	private boolean isMultipart(String contentType) {
		return contentType.contains("multipart/");
	}

	@SneakyThrows
	private HttpClient buildClient() {

		if (HttpOptionsPluginProvider.options().isHttp3Support()) {
			return new JettyHttp3Client();
		}

		Builder builder = HttpClient.newBuilder();
		if (!HttpOptionsPluginProvider.options().isHttp2Support()){
			builder.version(Version.HTTP_1_1);
		}

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

		setupSslLegacyProtocolSupport(builder);

		return builder
				.build();
	}

	private void setupSslLegacyProtocolSupport(Builder builder) {
		var sslParameters = new SSLParameters();
		sslParameters.setProtocols(new String[]{"SSLv3","TLSv1","TLSv1.1","TLSv1.2"});
		builder.sslParameters(sslParameters);
	}

	private void disableSsl(Builder builder) {

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = {
		    new X509TrustManager() {
		        @Override
				public X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        @Override
				public void checkClientTrusted(
		            X509Certificate[] certs, String authType) {
		        }
		        @Override
				public void checkServerTrusted(
		            X509Certificate[] certs, String authType) {
		        }
		    }
		};

		// Install the all-trusting trust manager
		try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new SecureRandom());

			builder.sslContext(sc);
		} catch (Exception e) {
			/* */
		}

	}
	@Override
	@SneakyThrows
	public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater, AsyncControl asyncControl) {
		HttpRequest httpRequest = toHttpRequest(request, templater);
		
		asyncControl.triggerReqeuestStarted();
		AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
		
		
		var chReq = new ChunkedRequest(buildClient(), httpRequest);
		chReq.executeRequest(asyncControl.onCancellationRequested);
		
		//we block until we get the headers:
		var responseInfo = chReq.getResponseInfo().get(); //TODO: timeout
		
		AtomicReference<ChunkedRequest> responseHolder = new AtomicReference<>(chReq);
		if (responseInfo.statusCode() == 407 
				&& HttpOptionsPluginProvider.options().isAskForProxyAuth()) {
			CountDownLatch latch = new CountDownLatch(1);
			Platform.runLater(() -> {
				CredentialsInputDialog dialog = new CredentialsInputDialog();
				dialog.showAndWait("Proxy Authentication" + getRealmInfo(responseInfo));
				if (!dialog.isCancelled()) {
					proxyCredentials = new PasswordAuthentication(dialog.getUsername(), dialog.getPassword().toCharArray());
					try {
						var newRequest = toHttpRequest(request, templater);
						startTime.set(System.currentTimeMillis());
						//TODO i actually need a new flux here, no?
						var proxyReq = new ChunkedRequest(buildClient(), newRequest);
						proxyReq.executeRequest(asyncControl.onCancellationRequested);
						responseHolder.set(proxyReq);
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
		
		chReq = responseHolder.get();
		chReq.getRequestDone().handle((res, e) -> {
//			System.out.println("triggers");
			if (e != null) {
				asyncControl.triggerRequestFailed(e);
			}
			else {
				asyncControl.triggerRequestSucceeded();
			}
			return null;
		});

		return toResponseContainer(httpRequest,
																chReq.getEmitterProcessor(),
																chReq.getResponseInfo(),
																startTime);
	}

	private String getRealmInfo(ResponseInfo responseInfo) {
		return responseInfo.headers()
							.firstValue("Proxy-Authenticate")
							.map(v -> realmPattern.matcher(v))
							.filter(m -> m.find())
							.map(m -> " (" + m.group(1) + ")")
							.orElse("");
	}

	@SneakyThrows
	private HttpRequest toHttpRequest(RestRequestContainer request, Templater templater) {
		HttpRequest.Builder builder = HttpRequest.newBuilder();
		builder.uri(new URI(HttpUtil.escapeUrl(request, templater)));

		request.getAspect(RestHeaderAspect.class)
		.ifPresent(aspect -> {
			aspect.getEntries().stream()
					.filter(HeaderEntry::isEnabled)
					.forEach(h -> builder.header(templater.replaceTags(h.getName()), templater.replaceTags(h.getValue())));
		});

		request.getAspect(RestBodyAspect.class)
		.ifPresent(aspect -> {
			if (request.getHttpMethod().equals("GET") || request.getHttpMethod().equals("DELETE")) {
				builder.method(request.getHttpMethod(), BodyPublishers.noBody());
			} else {
				var bodyContent = templater.replaceTags(aspect.getBody());
				var processedBodyContent = builder.build().headers().firstValue(CONTENT_TYPE_HEADER)
						.map(contentType -> RequestBodyPostProcessor.processBody(contentType, bodyContent))
						.orElse(bodyContent);
				builder.method(request.getHttpMethod(), BodyPublishers.ofString(processedBodyContent));
			}
		});

		if (builder.build().headers().firstValue(USER_AGENT_HEADER).isEmpty()) {
			builder.setHeader(USER_AGENT_HEADER, "Milkman");
		}

		if (proxyCredentials != null) {
			builder.setHeader(PROXY_AUTHORIZATION_HEADER, HttpUtil.authorizationHeaderValue(proxyCredentials));
		}


		return builder.build();
	}

	

	@SneakyThrows
	private RestResponseContainer toResponseContainer(HttpRequest request, Flux<byte[]> bodyPublisher, CompletableFuture<ResponseInfo> httpResponse, AtomicLong startTime) {
		RestResponseContainer response = new RestResponseContainer(request.uri().toString());
		response.getAspects().add(new RestResponseBodyAspect(bodyPublisher));

		addDebugOutput(request, response);


		var entries = httpResponse.thenApply(res -> {
			List<HeaderEntry> result = new LinkedList<>();
			for (Entry<String, List<String>> h : res.headers().map().entrySet()) {
				for(String hval : h.getValue()) {
					result.add(new HeaderEntry(UUID.randomUUID().toString(), h.getKey(), hval, false));
				}
			}
			return result;
		});
		RestResponseHeaderAspect headers = new RestResponseHeaderAspect(entries.get());
		
		response.getAspects().add(headers);
		
		httpResponse.thenAccept(res -> {
			var responseTimeInMs = System.currentTimeMillis() - startTime.get();
			buildStatusView(res, response, responseTimeInMs);
		});
		
		return response;
	}

	private void addDebugOutput(HttpRequest request, RestResponseContainer response) {
		if (CoreApplicationOptionsProvider.options().isDebug()) {
			var dheaders = new DebugRequestHeaderAspect();
			request.headers().map().forEach((k, vs) -> {
				vs.forEach(v -> {
					dheaders.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), k, v, true));
				});
			});
			response.getAspects().add(dheaders);

			request.bodyPublisher()
					.map(JdkFlowAdapter::flowPublisherToFlux)
					.map(flux -> flux.map(ByteBuffer::array))
					.map(flux -> new BlockingFluxByteToStringConverter().convert(flux))
					.filter(StringUtils::isNotBlank)
					.ifPresent(body -> response.getAspects().add(new DebugRequestBodyAspect(body)));
		}
	}

	private void buildStatusView(ResponseInfo httpResponse, RestResponseContainer response, long responseTimeInMs) {
		String versionStr = "undefined";
		if (httpResponse.version() == Version.HTTP_1_1) {
			versionStr = "1.1";
		} else if (httpResponse.version() == Version.HTTP_2) {
			versionStr = "2.0";
		} else if (httpResponse.version() == null) { //special case for custom http_3 implementation
			versionStr = "3.0";
		}
		LinkedHashMap<String, StyledText> statusKeys = new LinkedHashMap<>();
		statusKeys.put("Status", new StyledText(""+httpResponse.statusCode(), getStyle(httpResponse.statusCode())));
		statusKeys.put("Time", new StyledText(responseTimeInMs + "ms"));
		statusKeys.put("Http", new StyledText(versionStr));
		
		response.getStatusInformations().complete(statusKeys);
	}

	private String getStyle(int statusCode) {
		String style = "-fx-text-fill: ";
		if (statusCode < 300) {
			return style + "#257a35";
		}
		if (statusCode < 400) {
			return style + "#edc600";
		}
		if (statusCode < 500) {
			return style + "#fab237";
		}

		return style + "#fc3b14";
	}

	@RequiredArgsConstructor
	public static class StaticResponseInfo implements ResponseInfo {
		@Delegate(types = ResponseInfo.class)
		private final HttpResponse<?> response;
	}
	public static class EmptyResponseInfo implements ResponseInfo {
		@Override
		public int statusCode() {
			return 0;
		}

		@Override
		public HttpHeaders headers() {
			return HttpHeaders.of(Collections.emptyMap(), (s1,s2) -> true);
		}

		@Override
		public Version version() {
			return null;
		}
	}
}
