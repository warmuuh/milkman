package milkman.ui.plugin.rest;

import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Publisher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringSubscriber implements BodySubscriber<String> {

	final CompletableFuture<String> bodyCF = new CompletableFuture<>();
	@Getter
	final CompletableFuture<ResponseInfo> responseInfo = new CompletableFuture<>();
	Flow.Subscription subscription;
//    List<ByteBuffer> responseData = new CopyOnWriteArrayList<>();
	private final SubmissionPublisher<String> publisher;

	@Override
	public CompletionStage<String> getBody() {
		return bodyCF;
	}

	@Override
	public void onSubscribe(Flow.Subscription subscription) {
		this.subscription = subscription;
		subscription.request(1); // Request first item
	}
	
	public boolean hasSubscription() {
		return this.subscription != null;
	}

	@Override
	public void onNext(List<ByteBuffer> buffers) {
		System.out.println("-- onNext " + buffers);
		try {
			String chunk = asString(buffers);
			System.out.println("\tBuffer Content:\n" + chunk);
			publisher.submit(chunk);
		} catch (Exception e) {
			System.out.println("\tUnable to print buffer content");
			publisher.submit(e.toString());
		}
//      buffers.forEach(ByteBuffer::rewind); // Rewind after reading
//      responseData.addAll(buffers);

		subscription.request(1); // Request next item
	}

	@Override
	public void onError(Throwable throwable) {
		publisher.submit(throwable.toString());
		bodyCF.completeExceptionally(throwable);
		responseInfo.completeExceptionally(throwable);
	}

	@Override
	public void onComplete() {
//      bodyCF.complete(asString(responseData));
		bodyCF.complete("");
	}

	private String asString(List<ByteBuffer> buffers) {
		return new String(toBytes(buffers), StandardCharsets.UTF_8);
	}

	private byte[] toBytes(List<ByteBuffer> buffers) {
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

	public void cancel() {
		if (subscription != null) {
			subscription.cancel();
		}
		onError(new CancellationException("Cancelled"));
	}

	public BodySubscriber<String> onResponse(ResponseInfo responseInfo) {
		System.out.println("Response info available");
		this.responseInfo.complete(responseInfo);
		return this;
	}
}