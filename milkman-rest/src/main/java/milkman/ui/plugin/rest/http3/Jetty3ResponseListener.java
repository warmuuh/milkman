package milkman.ui.plugin.rest.http3;

import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SubmissionPublisher;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http.MetaData.Response;
import org.eclipse.jetty.http3.api.Stream;
import org.eclipse.jetty.http3.api.Stream.Client;
import org.eclipse.jetty.http3.frames.HeadersFrame;

@Slf4j
class Jetty3ResponseListener<T> implements Client.Listener {

  private final JettyHttp3Client jettyHttp3Client;
  private final URI uri;
  private final BodyHandler<T> responseBodyHandler;
  private final CompletableFuture<HttpResponse<T>> futureResponse;
  private BodySubscriber<T> bodySubscriber;
  private SubmissionPublisher publisher;
  private JettyHttpResponse<T> jettyHttpResponse;

  public Jetty3ResponseListener(JettyHttp3Client jettyHttp3Client, URI uri, BodyHandler<T> responseBodyHandler,
      CompletableFuture<HttpResponse<T>> futureResponse) {
    this.jettyHttp3Client = jettyHttp3Client;
    this.uri = uri;
    this.responseBodyHandler = responseBodyHandler;
    this.futureResponse = futureResponse;
    publisher = new SubmissionPublisher();
  }

  @Override
  public void onResponse(Client stream, HeadersFrame frame) {
    MetaData metaData = frame.getMetaData();
    Response response = (Response) metaData;
//    log.info("Received headers");
    bodySubscriber = responseBodyHandler.apply(new JettyResponseInfo(response));
    publisher.subscribe(bodySubscriber);
    jettyHttpResponse = new JettyHttpResponse<>(uri, response);

    if (!frame.isLast()) {
      // Demand to be called back.
      stream.demand();
    } else {
      futureResponse.complete(jettyHttpResponse);
      publisher.close();
      jettyHttp3Client.close();
    }


  }

  @Override
  public void onDataAvailable(Client stream) {
    // Read a chunk of the content.
    Stream.Data data = stream.readData();
    if (data == null) {
      // No data available now, demand to be called back.
      stream.demand();
    } else {
      // Process the content.
      //process(data.getByteBuffer());
//      log.info("Received data {}", data.getByteBuffer());
      //body receiver is expecting a list of byte[], see ChunkedRequest
      publisher.submit(List.of(data.getByteBuffer()));

      // Notify the implementation that the content has been consumed.
      data.complete();

      if (!data.isLast()) {
        // Demand to be called back.
        stream.demand();
      } else {
        futureResponse.complete(jettyHttpResponse);
        publisher.close();
        jettyHttp3Client.close();
      }
    }
  }
}
