package milkman.plugin.grpc.processor;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import lombok.RequiredArgsConstructor;
import me.dinowernli.grpc.polyglot.grpc.ServerReflectionClient;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.ui.plugin.Templater;
import reactor.core.publisher.FluxSink;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public class BaseGrpcProcessor {

	protected InetSocketAddress parseEndpoint(String endpoint) {
		String[] split = endpoint.split(":");
		if (split.length != 2) {
			throw new IllegalArgumentException("Unexpected endpoint format. should be 'host:port'");
		}
		InetSocketAddress addr = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
		return addr;
	}

	protected ManagedChannel createChannel(GrpcRequestContainer request, Templater templater) {
		InetSocketAddress endpoint = parseEndpoint(templater.replaceTags(request.getEndpoint()));
	    ManagedChannel channel = ManagedChannelBuilder.forAddress(endpoint.getHostName(), endpoint.getPort())
	            .usePlaintext()
	            .build();
		return channel;
	}
	
	protected FileDescriptorSet fetchServiceDescriptionViaReflection(Channel channel, String fullServiceName) throws InterruptedException, ExecutionException {
		var client = ServerReflectionClient.create(channel);
		FileDescriptorSet descriptorSet = client.lookupService(fullServiceName).get();
		return descriptorSet;
	}

	/**
	 * bridges to a publisher and handles channel closing 
	 *
	 */
	@RequiredArgsConstructor
	class StreamObserverToPublisherBridge<ReqT, ResT> implements ClientResponseObserver<ReqT, ResT> {
		private final FluxSink<ResT> publisher;
		private final Runnable onClose;
		private ClientCallStreamObserver<ReqT> requestStream;

		@Override
		public void onNext(ResT value) {
			publisher.next(value);
		}
		
		@Override
		public void onError(Throwable t) {
			onClose.run();
			publisher.error(t);
		}
		
		@Override
		public void onCompleted() {
			onClose.run();
			publisher.complete();
		}

		@Override
		public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
			this.requestStream = requestStream;
		}
		
		public void cancel() {
			requestStream.cancel("cancelled", new Exception("Cancellation Requested"));
		}
	}
}
