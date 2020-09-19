package milkman.ui.plugin.rest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.utils.Event0;
import org.apache.commons.lang3.exception.ExceptionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class ChunkedRequest {
	private final HttpClient httpclient; 
	private final HttpRequest httpRequest;
	
	@Getter
	private final CompletableFuture<ResponseInfo> responseInfo = new CompletableFuture<>(); 
	
	@Getter
	private CompletableFuture<Void> requestDone;
	
	private ReplayProcessor<byte[]> emitterProcessor;
	private Flux<byte[]> chunks;

	public void executeRequest(Event0 cancellationEvent){


		this.emitterProcessor = ReplayProcessor.create();
		chunks = emitterProcessor;
		this.requestDone = emitterProcessor.then().toFuture();
		AtomicBoolean isSubscribed = new AtomicBoolean();
		var future = httpclient.sendAsync(httpRequest, conInfo -> new HttpResponse.BodySubscriber<String>(){
			private Flow.Subscription subscription;

			{
				responseInfo.complete(conInfo);
				cancellationEvent.add(() -> {
					if (subscription != null)
						subscription.cancel();
					onComplete();
				});
			}

			@Override
			public void onSubscribe(Flow.Subscription subscription) {
				this.subscription = subscription;
				isSubscribed.set(true);
				subscription.request(1);
			}

			@Override
			public void onNext(List<ByteBuffer> item) {
				byte[] chunk = toBytes(item);
//				System.out.println("Received chunk: " + chunk);
				emitterProcessor.onNext(chunk);
				subscription.request(1);
			}

			@Override
			public void onError(Throwable throwable) {
				emitterProcessor.onError(throwable);
			}

			@Override
			public void onComplete() {
				emitterProcessor.onComplete();
			}

			@Override
			public CompletionStage<String> getBody() {
				return requestDone.thenApply(x -> "");
			}
		});

		future.handle((res, err) -> {
//				System.out.println("Received response: " + res);
//				System.out.println("Received err: " + err);
//				System.out.println("has subscription: " + isSubscribed.get());
				//under certain circumstances, the stringSubscriber was not subscribed (body handler not activated)
				//leading to the call-future resolve but the futures in the subscriber to not be resolved.
				if (!isSubscribed.get()) {
					if (err != null) {
						emitterProcessor.onError(ExceptionUtils.getRootCause(err));
						responseInfo.complete(new JavaRequestProcessor.EmptyResponseInfo());
					} else {
						emitterProcessor.onComplete();
						responseInfo.complete(new JavaRequestProcessor.StaticResponseInfo(res));
					}
				}
				return null;
			});
		
//		this.chunks = Flux.<String>create(emitter -> {
//			var stringSubscriber = new StringSubscriber(emitter);
//			var future =
//			future.handle((res, err) -> {
//				System.out.println("Received response: " + res);
//				System.out.println("Received err: " + err);
//				System.out.println("has subscription: " + stringSubscriber.hasSubscription());
//				//under certain circumstances, the stringSubscriber was not subscribed (body handler not activated)
//				//leading to the call-future resolve but the futures in the subscriber to not be resolved.
//				if (!stringSubscriber.hasSubscription()) {
//					if (err != null) {
//						emitter.error(err);
//					} else {
//						emitter.complete();
//						responseInfo.complete(new StaticResponseInfo(res));
//					}
//				}
//				return null;
//			});
//			stringSubscriber.getResponseInfo().thenAccept(responseInfo::complete);
//			cancellationEvent.add(stringSubscriber::cancel);
//		}).cache();
		

	}
	
	public Flux<byte[]> getEmitterProcessor(){
		return chunks;
	}


//	private static String asString(List<ByteBuffer> buffers) {
//		return new String(toBytes(buffers), StandardCharsets.UTF_8);
//	}

	private static byte[] toBytes(List<ByteBuffer> buffers) {
		int size = buffers.stream().mapToInt(ByteBuffer::remaining).sum();
		byte[] bs = new byte[size];
		int offset = 0;
		for (ByteBuffer buffer : buffers) {
			int remaining = buffer.remaining();
			buffer.get(bs, offset, remaining);
			offset += remaining;
		}
		return bs;
	}
}
