package milkman.ui.plugin.rest;

import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import lombok.SneakyThrows;
import milkman.domain.ResponseContainer;
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
import milkman.ui.plugin.rest.tls.CertificateReader;
import milkman.ui.plugin.rest.tls.CustomCertificateKeyManager;
import milkman.ui.plugin.rest.tls.TrustAllTrustManager;
import milkman.utils.AsyncResponseControl;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Socks5Proxy;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.client.transport.HttpClientTransportOverHTTP3;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.quic.client.ClientQuicConfiguration;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import reactor.core.publisher.Flux;

public class JettyRequestProcessor implements RequestProcessor {

  private static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
  private static final String CONTENT_TYPE_HEADER = "Content-Type";
  private static final String USER_AGENT_HEADER = "User-Agent";

  private static PasswordAuthentication proxyCredentials;
  private final Pattern realmPattern = Pattern.compile("realm=(\".*\")");

  @Override
  public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater,
                                              AsyncResponseControl.AsyncControl asyncControl) {


    HttpClient client = configureClient(request);
    try {
      client.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Request jettyRequest = toRequest(request, client, templater);
    asyncControl.triggerReqeuestStarted();
    AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    JettyResponseListener responseListener = new JettyResponseListener(asyncControl);
    jettyRequest.listener(responseListener);


    jettyRequest.send(responseListener);


    Runnable stopClient = () -> {
      try {
        client.stop();
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
    responseListener.getRequestDone().thenRun(stopClient);
    asyncControl.onCancellationRequested.add(() -> {
      jettyRequest.abort(new RuntimeException("Request was cancelled"))
          .thenRun(stopClient);
      asyncControl.triggerRequestFailed(new RuntimeException("Request was cancelled"));
    });

    List<JettyResponseListener.HeaderValue> headers = List.of();

    //TODO support 407 proxy auth retry
    //we block until we get the headers:
    try {
      headers = responseListener.getHeaders().get();
    } catch (InterruptedException e) {
      asyncControl.triggerRequestFailed(e);
    } catch (ExecutionException e) {
      asyncControl.triggerRequestFailed(e.getCause());
    }
    return toResponseContainer(jettyRequest, headers, responseListener, startTime);
  }

  private static RestResponseContainer toResponseContainer(Request request,
                                                           List<JettyResponseListener.HeaderValue> headers,
                                                           JettyResponseListener responseListener,
                                                           AtomicLong startTime) {
    RestResponseContainer response = new RestResponseContainer(request.getURI().toString());

    buildStatusView(responseListener, response);

    var bodyFlux = tapContentLength(responseListener.getChunks(), response);
    bodyFlux = bodyFlux.doOnComplete(() -> {
      response.getStatusInformations()
          .add("Time", (System.currentTimeMillis() - startTime.get()) + "ms");
      response.getStatusInformations().complete();
    });

    response.getAspects()
        .add(new RestResponseBodyAspect(bodyFlux));
    response.getAspects()
        .add(new RestResponseHeaderAspect(
            headers.stream().map(h -> new HeaderEntry(
                UUID.randomUUID().toString(), h.getName(), h.getValue(), true)).toList()));

    addDebugOutput(request, response);


    responseListener.getSslSessionInfo().thenAccept(ssl -> {
      ssl.ifPresent(sslSession ->
          response.getStatusInformations().add("SSL", getCertDetails(sslSession)));
    });

    return response;
  }


  private static Flux<byte[]> tapContentLength(Flux<byte[]> bodyPublisher,
                                               RestResponseContainer response) {
    AtomicLong byteCount = new AtomicLong(0);
    bodyPublisher = bodyPublisher.doOnNext(bytes -> {
      var curByteCount = byteCount.addAndGet(bytes.length);
      response.getStatusInformations().add("Details", Map.of("Size", "" + curByteCount));
    });
    return bodyPublisher;
  }

  @SneakyThrows
  private static Map<String, String> getCertDetails(SSLSession sslSession) {
    LinkedHashMap<String, String> result = new LinkedHashMap<>();
    result.put("Protocol", sslSession.getProtocol());
    Certificate cert = sslSession.getPeerCertificates()[0];
    result.put("Certificate type", cert.getType());
    if (cert instanceof X509Certificate) {
      var x509 = (X509Certificate) sslSession.getPeerCertificates()[0];
      result.put("Subject", x509.getSubjectX500Principal().getName());
      result.put("Valid from", x509.getNotBefore().toInstant().toString());
      result.put("Valid until", x509.getNotAfter().toInstant().toString());
    }
    return result;
  }

  private static void addDebugOutput(Request request, RestResponseContainer response) {
    if (CoreApplicationOptionsProvider.options().isDebug()) {
      var dheaders = new DebugRequestHeaderAspect();
      request.getHeaders().forEach(header -> {
        header.getValueList().forEach(v -> {
          dheaders.getEntries()
              .add(new HeaderEntry(UUID.randomUUID().toString(), header.getName(), v, true));
        });
      });
      response.getAspects().add(dheaders);

      if (request.getBody() instanceof StringRequestContent content) {
        boolean rewind = content.rewind();
        StringBuffer buffer = new StringBuffer();
        for (Content.Chunk chunk = content.read(); chunk != Content.Chunk.EOF;
             chunk = content.read()) {
          buffer.append(new String(chunk.getByteBuffer().array()));
        }
        response.getAspects().add(new DebugRequestBodyAspect(buffer.toString()));
      }
    }
  }

  private static void buildStatusView(JettyResponseListener responseListener,
                                      RestResponseContainer response) {
    responseListener.getResponseLineInfo().thenAccept(info -> {
      response.getStatusInformations()
          .add("Status", new ResponseContainer.StyledText("" + info.getStatus(),
              getStyle(info.getStatus())));

      String versionStr = "undefined";
      if (info.getVersion() == HttpVersion.HTTP_1_1) {
        versionStr = "1.1";
      } else if (info.getVersion() == HttpVersion.HTTP_2) {
        versionStr = "2.0";
      } else if (info.getVersion() == HttpVersion.HTTP_3) {
        versionStr = "3.0";
      }

      response.getStatusInformations()
          .add("Details", Map.of(
              "Http", versionStr
          ));
    });


    responseListener.getTtfb().thenAccept(ttfb -> {
      response.getStatusInformations().add("Details", Map.of(
          "TTFB", ttfb + "ms"
      ));
    });
  }


  private static String getStyle(int statusCode) {
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


  private Request toRequest(RestRequestContainer request, HttpClient client, Templater templater) {
    URI uri = encodeUrl(templater.replaceTags(request.getUrl()));
    Request jreq = client.newRequest(uri);
    jreq.method(request.getHttpMethod());

    request.getAspect(RestHeaderAspect.class)
        .ifPresent(aspect -> {
          jreq.headers(headers -> {
            aspect.getEntries().stream()
                .filter(HeaderEntry::isEnabled)
                .forEach(h -> headers.put(templater.replaceTags(h.getName()),
                    templater.replaceTags(h.getValue())));
          });
        });


    request.getAspect(RestBodyAspect.class)
        .ifPresent(aspect -> {
          if (!request.getHttpMethod().equals("GET") && !request.getHttpMethod().equals("DELETE")) {
            var bodyContent = templater.replaceTags(aspect.getBody());
            var mimeType = jreq.getHeaders().get(CONTENT_TYPE_HEADER);
            if (mimeType == null) {
              mimeType = "text/plain";
            }
            jreq.body(new StringRequestContent(mimeType, bodyContent));
          }
        });

    if (jreq.getHeaders().get(USER_AGENT_HEADER) == null) {
      jreq.headers(hs -> hs.add(USER_AGENT_HEADER, "Milkman"));
    }

    if (proxyCredentials != null) {
      jreq.headers(hs -> hs.add(PROXY_AUTHORIZATION_HEADER,
          HttpUtil.authorizationHeaderValue(proxyCredentials)));
    }

    return jreq;
  }

  private static URI encodeUrl(String urlAsStr) {
    //do proper url encoding, e.g. for special characters in path
    try {
      var url = new URL(urlAsStr);
      return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
              url.getQuery(), url.getRef());
    } catch (MalformedURLException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static HttpClient configureClient(RestRequestContainer request) {
    HttpClient client = new HttpClient(configureTransport(request));
    client.setFollowRedirects(HttpOptionsPluginProvider.options().isFollowRedirects());
    configureProxy(client);
    configureSocksProxy(client);

    return client;
  }

  @SneakyThrows
  private static void configureProxy(HttpClient client) {
    if (HttpOptionsPluginProvider.options().isUseProxy()) {
      URL url = new URL(HttpOptionsPluginProvider.options().getProxyUrl());
      boolean secure = Objects.equals(url.getProtocol(), "https");
      int port = url.getPort() < 0
          ? (secure ? 443 : 80)
          : url.getPort();
      HttpProxy proxy = new HttpProxy(new Origin.Address(url.getHost(), port), secure);
      List<String> exclusions =
          Arrays.asList(HttpOptionsPluginProvider.options().getProxyExclusion().split("\\|"));
      proxy.getExcludedAddresses().addAll(exclusions.stream().filter(s -> !s.isBlank()).toList());
      client.getProxyConfiguration().getProxies().add(proxy);
    }
  }

  private static HttpClientTransport configureTransport(RestRequestContainer request) {
    ClientConnector connector = configureSslConnector(request);

    if (HttpOptionsPluginProvider.options().isHttp2Support()) {
      HTTP2Client http2Client = new HTTP2Client(connector);
      return new HttpClientTransportOverHTTP2(http2Client);
    } else if (HttpOptionsPluginProvider.options().isHttp3Support()) {
      ClientQuicConfiguration clientQuicConfig =
          new ClientQuicConfiguration(connector.getSslContextFactory(), null);

      HTTP3Client http3Client = new HTTP3Client(clientQuicConfig, connector);
      return new HttpClientTransportOverHTTP3(http3Client);
    } else if (HttpOptionsPluginProvider.options().isHttpProtocolAuto()) {
      return new HttpClientTransportDynamic(connector);
    }

    // default to HTTP/1.1
    return new HttpClientTransportOverHTTP(connector);
  }

  private static ClientConnector configureSslConnector(RestRequestContainer request) {

    //TODO support legacy ssl protocols "SSLv3","TLSv1","TLSv1.1","TLSv1.2"

    SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();


    TrustManager[] trustManagers = null;
    KeyManager[] keyManagers = null;

    if (!HttpOptionsPluginProvider.options().isCertificateValidation()) {
      // Create a trust manager that does not validate certificate chains
      trustManagers = new TrustManager[]{
          new TrustAllTrustManager()
      };
    }


    //setup client cert if necessary
    if (StringUtils.isNotEmpty(request.getClientCertificate())) {
      CertificateReader certReader = new CertificateReader();
      milkman.ui.plugin.rest.tls.Certificate cert =
          HttpOptionsPluginProvider.options().getCertificates().stream()
              .filter(c -> c.getName().equals(request.getClientCertificate()))
              .findAny()
              .orElseThrow(() -> new IllegalStateException(
                  "Certificate not found: " + request.getClientCertificate()));

      try {
        var x509Certificate = certReader.readCertificate(cert);
        var privateKey = certReader.readPrivateKey(cert);
        if (privateKey != null && x509Certificate != null) {
          keyManagers =  new KeyManager[]{
              new CustomCertificateKeyManager(x509Certificate, privateKey)
          };
        }

      } catch (Exception e) {
        throw new RuntimeException("Failed to load client certificate", e);
      }


      try {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(keyManagers, trustManagers, new SecureRandom());
        sslContextFactory.setSslContext(sc);
        sslContextFactory.setIncludeProtocols("SSLv3","TLSv1","TLSv1.1","TLSv1.2");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    ClientConnector connector = new ClientConnector();
    connector.setSslContextFactory(sslContextFactory);
    return connector;
  }

  private static void configureSocksProxy(HttpClient client) {
    if (CoreApplicationOptionsProvider.options().isUseSocksProxy()) {
      String socksProxyAddress = CoreApplicationOptionsProvider.options().getSocksProxyAddress();
      String[] parts = socksProxyAddress.split(":");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid socks proxy address: " + socksProxyAddress);
      }
      String host = parts[0];
      int port = Integer.parseInt(parts[1]);
      Socks5Proxy proxy = new Socks5Proxy(host, port);
      client.getProxyConfiguration().addProxy(proxy);
    }
  }
}
