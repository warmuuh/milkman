package milkman.ui.plugin.rest.http3;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.net.http.HttpResponse.ResponseInfo;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http.MetaData.Response;
import org.eclipse.jetty.http3.api.Session;
import org.eclipse.jetty.http3.api.Stream;
import org.eclipse.jetty.http3.api.Stream.Client;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.frames.HeadersFrame;
import reactor.core.publisher.ReplayProcessor;

@Slf4j
public class JettyHttp3Client extends HttpClient {

  private final HTTP3Client httpClient;
  private long timeoutInMillis;

  public JettyHttp3Client() throws Exception {
    httpClient = new HTTP3Client();
    // Configure HTTP3Client, for example:
    httpClient.getHTTP3Configuration().setStreamIdleTimeout(15000);
    httpClient.start();
  }

  @Override
  public Optional<CookieHandler> cookieHandler() {
    return Optional.empty();
  }

  @Override
  public Optional<Duration> connectTimeout() {
    return Optional.empty();
  }

  @Override
  public Redirect followRedirects() {
    return null;
  }

  @Override
  public Optional<ProxySelector> proxy() {
    return Optional.empty();
  }

  @Override
  public SSLContext sslContext() {
    return null;
  }

  @Override
  public SSLParameters sslParameters() {
    return null;
  }

  @Override
  public Optional<Authenticator> authenticator() {
    return Optional.empty();
  }

  @Override
  public Version version() {
    return null;
  }

  @Override
  public Optional<Executor> executor() {
    return Optional.empty();
  }

  @Override
  public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
      throws IOException, InterruptedException {
    CompletableFuture<HttpResponse<T>> futureResponse = sendAsync(request, responseBodyHandler);
    try {
      return futureResponse.get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else if (e.getCause() instanceof InterruptedException) {
        throw (InterruptedException) e.getCause();
      } else {
        throw new RuntimeException(e.getCause());
      }
    }
  }

  @Override
  @SneakyThrows
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
      BodyHandler<T> responseBodyHandler) {
    CompletableFuture<HttpResponse<T>> futureResponse = new CompletableFuture<>();
    String method = request.method();
    URI uri = request.uri();
    int port = uri.getPort();
    if (port < 0) {
      if (uri.getScheme().equals("http")) {
        port = 80;
      } else if (uri.getScheme().equals("https")) {
        port = 443;
      }
    }
    SocketAddress serverAddress = new InetSocketAddress(uri.getHost(), port);

    CompletableFuture<Session.Client> sessionCF = httpClient.connect(serverAddress, new Session.Client.Listener() {});
    Session.Client session = sessionCF.get();

    HttpFields.Mutable requestHeaders = HttpFields.build();
    // Add request headers
    request.headers().map().forEach((name, values) -> {
      for (String value : values) {
        requestHeaders.put(name, value);
      }
    });
    MetaData.Request jettyRequest = new MetaData.Request("GET", HttpURI.from(uri), HttpVersion.HTTP_3, requestHeaders);

    // The HTTP/3 HEADERS frame, with endStream=true
    // to signal that this request has no content.
    HeadersFrame headersFrame = new HeadersFrame(jettyRequest, true);

    // Open a Stream by sending the HEADERS frame.
    session.newRequest(headersFrame, new Stream.Client.Listener() {

      private BodySubscriber<T> bodySubscriber;
      private SubmissionPublisher publisher = new SubmissionPublisher();
      private JettyHttpResponse<T> jettyHttpResponse;

      @Override
      public void onResponse(Client stream, HeadersFrame frame) {
        MetaData metaData = frame.getMetaData();
        MetaData.Response response = (MetaData.Response)metaData;
        log.info("Received headers");

        //assumes that HEADERS are always sent first
        bodySubscriber = responseBodyHandler.apply(new JettyResponseInfo(response));
        publisher.subscribe(bodySubscriber);
        jettyHttpResponse = new JettyHttpResponse<>(uri, response);


        if (!frame.isLast())
        {
          // Demand to be called back.
          stream.demand();
        } else {
          log.info("closing connection (no body)");
          futureResponse.complete(jettyHttpResponse);
          publisher.close();
          close();
        }



      }

      @Override
      public void onDataAvailable(Client stream) {
        // Read a chunk of the content.
        Stream.Data data = stream.readData();
        if (data == null)
        {
          // No data available now, demand to be called back.
          stream.demand();
        }
        else
        {

          // Process the content.
          //process(data.getByteBuffer());
          log.info("Received data {}", data.getByteBuffer());
          publisher.submit(List.of(data.getByteBuffer()));

          // Notify the implementation that the content has been consumed.
          data.complete();

          if (!data.isLast())
          {
            // Demand to be called back.
            stream.demand();
          } else {
            log.info("closing connection");
            futureResponse.complete(jettyHttpResponse);
            publisher.close();
            close();
          }
        }
      }
    });

    // Set request body
    //requestBodyPublisher(request, jettyRequest);

    return futureResponse;
  }

  @Override
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler,
      PushPromiseHandler<T> pushPromiseHandler) {
    return null;
  }

//  private void requestBodyPublisher(HttpRequest request, HttpRequest jettyRequest) {
//    if (request.bodyPublisher().isPresent()) {
//      request.bodyPublisher().get().subscribe(new BodySubscriberAdapter(jettyRequest));
//    }
//  }

  public void close() {
    try {
      httpClient.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public HttpClient connectTimeout(long timeout, TimeUnit unit) {
    this.timeoutInMillis = unit.toMillis(timeout);
    return this;
  }

  public HttpClient followRedirects(Redirect followRedirects) {
//    switch (followRedirects) {
//      case ALWAYS:
//        httpClient.setFollowRedirects(HttpClient.FOLLOW_ALWAYS);
//        break;
//      case NEVER:
//        httpClient.setFollowRedirects(HttpClient.FOLLOW_NEVER);
//        break;
//      case NORMAL:
//        httpClient.setFollowRedirects(HttpClient.FOLLOW_NORMAL);
//        break;
//      default:
//        throw new IllegalArgumentException("Unknown Redirect value: " + followRedirects);
//    }
    return this;
  }

//  @Override
//  public HttpClient version(Version version) {
//    // HTTP/3 is not yet a standard version in java.net.http.HttpClient.
//    // The only supported versions are HTTP/1.1 and HTTP/2.
//    return this;
//  }

  //  private static class BodySubscriberAdapter implements Flow.Subscriber<ByteBuffer> {
//
//    private final Request jettyRequest;
//
//    public BodySubscriberAdapter(Request jettyRequest) {
//      this.jettyRequest = jettyRequest;
//    }
//
//    @Override
//    public void onSubscribe(Flow.Subscription subscription) {
//      subscription.request(Long.MAX_VALUE);
//    }
//
//    @Override
//    public void onNext(ByteBuffer item) {
//      jettyRequest.content(new org.eclipse.jetty.client.util.ByteBufferContentProvider(item));
//    }
//
//    @Override
//    public void onError(Throwable throwable) {
//      throw new RuntimeException(throwable);
//    }
//
//    @Override
//    public void onComplete() {
//      // Do nothing
//    }
//  }

  @Value
  class JettyResponseInfo implements ResponseInfo {

    MetaData.Response responseInfo;

    @Override
    public int statusCode() {
      return responseInfo.getStatus();
    }

    @Override
    public HttpHeaders headers() {
      Map<String, List<String>> headers = new HashMap<>();
      responseInfo.getFields().forEach(header ->
          headers.computeIfAbsent(header.getName(), name -> new LinkedList<>())
              .add(header.getValue()));

      return HttpHeaders.of(headers, (a, b) -> true);
    }

    @Override
    public Version version() {
      return null;
    }
  }

}

