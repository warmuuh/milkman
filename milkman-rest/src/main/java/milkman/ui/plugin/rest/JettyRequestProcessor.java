package milkman.ui.plugin.rest;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseContainer;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import milkman.ui.plugin.rest.tls.CertificateReader;
import milkman.utils.AsyncResponseControl;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.Socks5Proxy;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.client.transport.HttpClientTransportOverHTTP3;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.quic.client.ClientQuicConfiguration;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import reactor.core.publisher.Flux;

public class JettyRequestProcessor implements RequestProcessor {
  @Override
  public RestResponseContainer executeRequest(RestRequestContainer request, Templater templater,
                                              AsyncResponseControl.AsyncControl asyncControl) {

    RestResponseContainer responseContainer = new RestResponseContainer(request.getUrl());
    try (HttpClient client = configureClient(request)) {
      client.start();
      asyncControl.triggerReqeuestStarted();
      ContentResponse response = client.newRequest(templater.replaceTags(request.getUrl()))
          .method(request.getHttpMethod())
          .send();
      asyncControl.triggerRequestSucceeded();


      responseContainer.getStatusInformations().add("status", "" + response.getStatus());
      responseContainer.getAspects()
          .add(new RestResponseBodyAspect(Flux.fromArray(new byte[][] {response.getContent()})));
      responseContainer.getAspects()
          .add(new RestResponseHeaderAspect(response.getHeaders().stream().map(h -> new HeaderEntry(
              UUID.randomUUID().toString(), h.getName(), h.getValue(), true)).toList()));
    } catch (Exception e) {
      asyncControl.triggerRequestFailed(e);
    }


    return responseContainer;
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
      CertificateReader certReader = new CertificateReader();
      milkman.ui.plugin.rest.tls.Certificate cert = HttpOptionsPluginProvider.options().getCertificates().stream()
          .filter(c -> c.getName().equals(request.getClientCertificate()))
          .findAny()
          .orElseThrow(() -> new IllegalStateException("Certificate not found: " + request.getClientCertificate()));

      var x509Certificate = certReader.readCertificate(cert);
      var privateKey = certReader.readPrivateKey(cert);
      //TODO: how to set client cert?
    }

    ClientConnector connector = new ClientConnector();
    connector.setSslContextFactory(sslContextFactory);
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
