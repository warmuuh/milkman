//package milkman.ui.plugin.rest.http3;
//
//import java.io.IOException;
//import java.net.Authenticator;
//import java.net.CookieHandler;
//import java.net.InetSocketAddress;
//import java.net.ProxySelector;
//import java.net.SocketAddress;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.net.http.HttpResponse.BodyHandler;
//import java.net.http.HttpResponse.PushPromiseHandler;
//import java.nio.ByteBuffer;
//import java.time.Duration;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Flow;
//import java.util.concurrent.TimeUnit;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLParameters;
//import lombok.Data;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.eclipse.jetty.http.HttpFields;
//import org.eclipse.jetty.http.HttpURI;
//import org.eclipse.jetty.http.HttpVersion;
//import org.eclipse.jetty.http.MetaData.Request;
//import org.eclipse.jetty.http3.api.Session;
//import org.eclipse.jetty.http3.api.Stream;
//import org.eclipse.jetty.http3.client.HTTP3Client;
//import org.eclipse.jetty.http3.frames.DataFrame;
//import org.eclipse.jetty.http3.frames.GoAwayFrame;
//import org.eclipse.jetty.http3.frames.HeadersFrame;
//import org.eclipse.jetty.http3.frames.SettingsFrame;
//
//@Slf4j
//public class JettyHttp3Client extends HttpClient {
//
//  private final HTTP3Client httpClient;
//  private long timeoutInMillis;
//
//  public JettyHttp3Client() throws Exception {
//    httpClient = new HTTP3Client();
//    // Configure HTTP3Client, for example:
//    httpClient.getHTTP3Configuration().setStreamIdleTimeout(15000);
//    httpClient.start();
//  }
//
//  @Override
//  public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
//      throws IOException, InterruptedException {
//    CompletableFuture<HttpResponse<T>> futureResponse = sendAsync(request, responseBodyHandler);
//    try {
//      return futureResponse.get();
//    } catch (ExecutionException e) {
//      if (e.getCause() instanceof IOException) {
//        throw (IOException) e.getCause();
//      } else if (e.getCause() instanceof InterruptedException) {
//        throw (InterruptedException) e.getCause();
//      } else {
//        throw new RuntimeException(e.getCause());
//      }
//    }
//  }
//
//  @Override
//  @SneakyThrows
//  public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler) {
//    URI uri = request.uri();
//    SocketAddress serverAddress = new InetSocketAddress(uri.getHost(), getPort(uri));
//
//    CompletableFuture<HttpResponse<T>> futureResponse = new CompletableFuture<>();
//    Jetty3ResponseListener<T> responseListener = new Jetty3ResponseListener<>(this, uri, responseBodyHandler, futureResponse);
//
//    CompletableFuture<Session.Client> sessionCF = httpClient.connect(serverAddress, new Session.Client.Listener() {
//      @Override
//      public void onFailure(Session session, long error, String reason, Throwable failure) {
//        System.out.println("Failure: " + failure);
//        System.out.println("Failure Reason: " + reason);
//      }
//
//      @Override
//      public void onSettings(Session session, SettingsFrame frame) {
//        System.out.println("Settings: " + frame);
//      }
//
//      @Override
//      public void onDisconnect(Session session, long error, String reason) {
//        System.out.println("Disconnect: " + reason + "(error: " + error + ")");
//      }
//
//      @Override
//      public void onGoAway(Session session, GoAwayFrame frame) {
//        System.out.println("Go Away: " + frame);
//      }
//
//    });
//
//    CompletableFuture<Stream> streamCF = sessionCF.thenCompose(session -> {
//      // Add request headers
//      HttpFields requestHeaders = buildHeaders(request, uri);
//
//      String path = uri.toString();
//      if (StringUtils.isEmpty(uri.getPath()) && !path.endsWith("/")) {
//        path += "/";
//      }
//
//      Request jettyRequest = new Request(request.method(), HttpURI.from(path), HttpVersion.HTTP_3, requestHeaders);
//      HeadersFrame headersFrame = new HeadersFrame(jettyRequest, !hasBody(request));
//      // Open a Stream by sending the HEADERS frame.
//      return session.newRequest(headersFrame, responseListener);
//    });
//
//    // Set request body
//    //TODO: do this in async chain
//    requestBodyPublisher(request, streamCF);
//
//    CompletableFuture<HttpResponse<T>> result = streamCF.thenCombine(futureResponse,
//        (stream, response) -> response);
//
//    //register cancellation callback
//    result.whenComplete((c,t) -> {
//      if (t != null) {
//        close();
//      }
//    });
//
//    return result;
//  }
//
//  private static int getPort(URI uri) {
//    int port = uri.getPort();
//    if (port < 0) {
//      if (uri.getScheme().equals("http")) {
//        port = 80;
//      } else if (uri.getScheme().equals("https")) {
//        port = 443;
//      }
//    }
//    return port;
//  }
//
//  private static HttpFields buildHeaders(HttpRequest request, URI uri) {
//    HttpFields.Mutable requestHeaders = HttpFields.build();
//    request.headers().map().forEach((name, values) -> {
//      for (String value : values) {
//        requestHeaders.put(name, value);
//      }
//    });
//
//    return requestHeaders;
//  }
//
//  private static boolean hasBody(HttpRequest request) {
//    return request.bodyPublisher().isPresent() && request.bodyPublisher().get().contentLength() > 0;
//  }
//
//  @Override
//  public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler,
//      PushPromiseHandler<T> pushPromiseHandler) {
//    return null;
//  }
//
//  private void requestBodyPublisher(HttpRequest request, CompletableFuture<Stream> streamCF) {
//    if (hasBody(request)) {
//      BodySubscriberAdapter bodySubscriberAdapter = new BodySubscriberAdapter(streamCF);
//      request.bodyPublisher().get().subscribe(bodySubscriberAdapter);
//      //TODO: do i need to wait somehow for bodySubscriberAdapter-futures?
//    }
//  }
//
//  public void close() {
//    //hack: when immediatly disconnecting bc of "session closed remotely", the selector is not yet in a state to handle stopping
//    // so .stop() hangs indefinitely. we simply wait a second and try it then...seems to work.
//    try {
//      new Thread(() -> {
//        try {
//          Thread.sleep(1000);
//          httpClient.stop();
//        } catch (Exception e) {
//          System.out.println("client stop failed: " + e);
//          throw new RuntimeException(e);
//        }
//      }).start();
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  public HttpClient connectTimeout(long timeout, TimeUnit unit) {
//    this.timeoutInMillis = unit.toMillis(timeout);
//    return this;
//  }
//
//  @Override
//  public Optional<CookieHandler> cookieHandler() {
//    return Optional.empty();
//  }
//
//  @Override
//  public Optional<Duration> connectTimeout() {
//    return Optional.empty();
//  }
//
//  @Override
//  public Redirect followRedirects() {
//    return null;
//  }
//
//  @Override
//  public Optional<ProxySelector> proxy() {
//    return Optional.empty();
//  }
//
//  @Override
//  public SSLContext sslContext() {
//    return null;
//  }
//
//  @Override
//  public SSLParameters sslParameters() {
//    return null;
//  }
//
//  @Override
//  public Optional<Authenticator> authenticator() {
//    return Optional.empty();
//  }
//
//  @Override
//  public Version version() {
//    return null;
//  }
//
//  @Override
//  public Optional<Executor> executor() {
//    return Optional.empty();
//  }
//
//
//  public HttpClient followRedirects(Redirect followRedirects) {
////    switch (followRedirects) {
////      case ALWAYS:
////        httpClient.setFollowRedirects(HttpClient.FOLLOW_ALWAYS);
////        break;
////      case NEVER:
////        httpClient.setFollowRedirects(HttpClient.FOLLOW_NEVER);
////        break;
////      case NORMAL:
////        httpClient.setFollowRedirects(HttpClient.FOLLOW_NORMAL);
////        break;
////      default:
////        throw new IllegalArgumentException("Unknown Redirect value: " + followRedirects);
////    }
//    return this;
//  }
//
////  @Override
////  public HttpClient version(Version version) {
////    // HTTP/3 is not yet a standard version in java.net.http.HttpClient.
////    // The only supported versions are HTTP/1.1 and HTTP/2.
////    return this;
////  }
//
//  @Data
//  private static class BodySubscriberAdapter implements Flow.Subscriber<ByteBuffer> {
//
//    public BodySubscriberAdapter(CompletableFuture<Stream> resultFuture) {
//      this.resultFuture = resultFuture;
//    }
//
//    private CompletableFuture<Stream> resultFuture;
//    private CompletableFuture<Void> finishedFuture = new CompletableFuture<>();
//
//    @Override
//    public void onSubscribe(Flow.Subscription subscription) {
//      subscription.request(Long.MAX_VALUE);
//    }
//
//    @Override
//    public void onNext(ByteBuffer item) {
//      resultFuture = resultFuture.thenCompose(stream -> stream.data(new DataFrame(item, false)));
//    }
//
//    @Override
//    public void onError(Throwable throwable) {
//      throw new RuntimeException(throwable);
//    }
//
//    @Override
//    public void onComplete() {
//      resultFuture = resultFuture.thenCompose(stream -> stream.data(new DataFrame(ByteBuffer.allocate(0), true)));
//      finishedFuture.complete(null);
//    }
//  }
//
//}
//
