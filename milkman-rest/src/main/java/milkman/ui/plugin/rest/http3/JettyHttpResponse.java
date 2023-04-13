package milkman.ui.plugin.rest.http3;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import org.eclipse.jetty.http.MetaData;

class JettyHttpResponse<T> implements HttpResponse<T> {

  private URI uri;
  private final MetaData.Response response;

  public JettyHttpResponse(URI uri, MetaData.Response response) {
    this.uri = uri;
    this.response = response;
  }

  @Override
  public int statusCode() {
    return response.getStatus();
  }

  @Override
  public HttpRequest request() {
    return null;
  }

  @Override
  public Optional<HttpResponse<T>> previousResponse() {
    return Optional.empty();
  }

  @Override
  public HttpHeaders headers() {
    Map<String, List<String>> headers = new HashMap<>();
    response.getFields().forEach(header ->
        headers.computeIfAbsent(header.getName(), name -> new LinkedList<>())
            .add(header.getValue()));

    return HttpHeaders.of(headers, (a, b) -> true);
  }

  @Override
  public T body() {
    return null;
//      try {
//        return response.getContent().array();
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      }
  }

  @Override
  public Optional<SSLSession> sslSession() {
    return Optional.empty();
  }

  @Override
  public URI uri() {
    return uri;
  }

  @Override
  public HttpClient.Version version() {
    return HttpClient.Version.HTTP_2;
  }
}
