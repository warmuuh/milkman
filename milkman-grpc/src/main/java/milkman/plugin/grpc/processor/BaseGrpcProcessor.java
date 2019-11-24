package milkman.plugin.grpc.processor;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SubmissionPublisher;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import me.dinowernli.grpc.polyglot.grpc.ServerReflectionClient;
import me.dinowernli.grpc.polyglot.protobuf.ProtoMethodName;
import milkman.plugin.grpc.domain.GrpcRequestContainer;

public class BaseGrpcProcessor {

	protected InetSocketAddress parseEndpoint(String endpoint) {
		String[] split = endpoint.split(":");
		if (split.length != 2) {
			throw new IllegalArgumentException("Unexpected endpoint format. should be 'host:port'");
		}
		InetSocketAddress addr = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
		return addr;
	}

	protected ManagedChannel createChannel(GrpcRequestContainer request) {
		InetSocketAddress endpoint = parseEndpoint(request.getEndpoint());
	    ManagedChannel channel = ManagedChannelBuilder.forAddress(endpoint.getHostName(), endpoint.getPort())
	            .usePlaintext()
	            .build();
		return channel;
	}

	protected FileDescriptorSet fetchServiceDescriptionViaReflection(ManagedChannel channel, ProtoMethodName protoMethod) throws InterruptedException, ExecutionException {
		var client = ServerReflectionClient.create(channel);
		FileDescriptorSet descriptorSet = client.lookupService(protoMethod.getFullServiceName()).get();
		return descriptorSet;
	}

	/**
	 * bridges to a publisher and handles channel closing 
	 *
	 * @param <T>
	 */
	@RequiredArgsConstructor
	class StreamObserverToPublisherBridge<T> implements StreamObserver<T> {
		private final SubmissionPublisher<T> publisher;
		private final ManagedChannel channel;

		@Override
		public void onNext(T value) {
			publisher.submit(value);
		}
		
		@Override
		public void onError(Throwable t) {
			t.printStackTrace();
			channel.shutdown();
			publisher.closeExceptionally(t);
		}
		
		@Override
		public void onCompleted() {
			channel.shutdown();
			publisher.close();
		}
	}
}
