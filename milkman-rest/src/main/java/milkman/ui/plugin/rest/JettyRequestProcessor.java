package milkman.ui.plugin.rest;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
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
import milkman.utils.AsyncResponseControl;
import milkman.utils.json.BlockingFluxByteToStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
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
import reactor.adapter.JdkFlowAdapter;
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


    try (HttpClient client = configureClient(request)) {
      client.start();
      Request jettyRequest = toRequest(request, client, templater);
      asyncControl.triggerReqeuestStarted();
      AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

      //TODO support async chuncked execution
      //TODO support 407 proxy auth retry
      ContentResponse response = jettyRequest.send();


      asyncControl.triggerRequestSucceeded();


      return toResponseContainer(jettyRequest, response, startTime);
    } catch (Exception e) {
      asyncControl.triggerRequestFailed(e);
      throw new RuntimeException("Failed to execute request", e);
    }
  }

  private static RestResponseContainer toResponseContainer(Request request,
                                                           ContentResponse httpResponse,
                                                           AtomicLong startTime) {
    RestResponseContainer response = new RestResponseContainer(request.getURI().toString());
    response.getStatusInformations().add("status", "" + httpResponse.getStatus());
    response.getStatusInformations()
        .add("Time", (System.currentTimeMillis() - startTime.get()) + "ms");

    response.getStatusInformations()
        .add("Details", Map.of("Size", "" + httpResponse.getContent().length));


    response.getAspects()
        .add(new RestResponseBodyAspect(Flux.fromArray(new byte[][] {httpResponse.getContent()})));
    response.getAspects()
        .add(new RestResponseHeaderAspect(
            httpResponse.getHeaders().stream().map(h -> new HeaderEntry(
                UUID.randomUUID().toString(), h.getName(), h.getValue(), true)).toList()));

    addDebugOutput(request, response);

    //TODO: add ssl status information
    //TODO fix ttfb
    buildStatusView(httpResponse, response, System.currentTimeMillis() - startTime.get());
    response.getStatusInformations().complete();

    return response;
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

  private static void buildStatusView(Response httpResponse, RestResponseContainer response,
                                      long ttfbInMs) {
    String versionStr = "undefined";
    if (httpResponse.getVersion() == HttpVersion.HTTP_1_1) {
      versionStr = "1.1";
    } else if (httpResponse.getVersion() == HttpVersion.HTTP_2) {
      versionStr = "2.0";
    } else if (httpResponse.getVersion() == HttpVersion.HTTP_3) {
      versionStr = "3.0";
    }
    response.getStatusInformations()
        .add("Status", new ResponseContainer.StyledText("" + httpResponse.getStatus(),
            getStyle(httpResponse.getStatus())))
        .add("Details", Map.of(
            "TTFB", ttfbInMs + "ms",
            "Http", versionStr
        ));
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
    Request jreq = client.newRequest(templater.replaceTags(request.getUrl()));
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
            jreq.body(new StringRequestContent(bodyContent, mimeType));
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
    if (!HttpOptionsPluginProvider.options().isCertificateValidation()) {
      sslContextFactory.setTrustAll(true);
    }

    //setup client cert if necessary
    if (StringUtils.isNotEmpty(request.getClientCertificate())) {
//      CertificateReader certReader = new CertificateReader();
//      milkman.ui.plugin.rest.tls.Certificate cert =
//          HttpOptionsPluginProvider.options().getCertificates().stream()
//              .filter(c -> c.getName().equals(request.getClientCertificate()))
//              .findAny()
//              .orElseThrow(() -> new IllegalStateException(
//                  "Certificate not found: " + request.getClientCertificate()));
//
//      var x509Certificate = certReader.readCertificate(cert);
//      var privateKey = certReader.readPrivateKey(cert);
      //TODO: how to set client cert?
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
