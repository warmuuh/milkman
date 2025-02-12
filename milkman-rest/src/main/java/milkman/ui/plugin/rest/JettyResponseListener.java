package milkman.ui.plugin.rest;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.net.ssl.SSLSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import milkman.utils.AsyncResponseControl;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.EndPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

@RequiredArgsConstructor
public class JettyResponseListener implements Response.Listener, Request.Listener {
  private long startRequestTime;

  private ReplayProcessor<byte[]> emitterProcessor = ReplayProcessor.create();

  private final AsyncResponseControl.AsyncControl asyncControl;

  @Getter
  private final CompletableFuture<List<HeaderValue>> headers = new CompletableFuture<>();
  @Getter
  private final CompletableFuture<Integer> ttfb = new CompletableFuture<>();

  @Getter
  private final CompletableFuture<ResponseLineInfo> responseLineInfo = new CompletableFuture<>();

  @Getter
  private final CompletableFuture<Optional<SSLSession>> sslSessionInfo = new CompletableFuture<>();

  @Getter
  private CompletableFuture<Void> requestDone = emitterProcessor.then().toFuture();


  @Override
  public void onCommit(Request request) {
    startRequestTime = System.currentTimeMillis();
  }

  @Override
  public void onBegin(Response response) {
    ttfb.complete((int) (System.currentTimeMillis() - startRequestTime));
    responseLineInfo.complete(new ResponseLineInfo(response.getVersion(), response.getStatus()));
    sslSessionInfo.complete(
        Optional.ofNullable(response.getRequest().getConnection().getSslSessionData())
        .map(EndPoint.SslSessionData::sslSession));
  }

  @Override
  public void onHeaders(Response response) {
    headers.complete(response.getHeaders().stream()
            .map(h -> new HeaderValue(h.getName(), h.getValue()))
            .toList());
  }

  @Override
  public void onSuccess(Response response) {
    emitterProcessor.onComplete();
    asyncControl.triggerRequestSucceeded();
  }

  @Override
  public void onFailure(Response response, Throwable failure) {
    emitterProcessor.onError(failure);
    asyncControl.triggerRequestFailed(failure);
    //complete all futures exceptionally to make sure they are not blocking
    headers.completeExceptionally(failure);
    ttfb.completeExceptionally(failure);
    responseLineInfo.completeExceptionally(failure);
    sslSessionInfo.completeExceptionally(failure);
  }

  @Override
  public void onContent(Response response, ByteBuffer content) {
    if (content.remaining() == 0) {
      return;
    }

    emitterProcessor.onNext(copyToArray(content));
  }

  Flux<byte[]> getChunks() {
    return emitterProcessor;
  }

  @Value
  public static class ResponseLineInfo {
    HttpVersion version;
    int status;
  }

  @Value
  public static class HeaderValue {
    String name;
    String value;
  }

  byte[] copyToArray(ByteBuffer buffer) {
    byte[] arr = new byte[buffer.remaining()];
    buffer.get(arr);
    return arr;
  }

}
