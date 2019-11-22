package milkman.plugin.grpc.processor;

import com.google.common.base.Joiner;

import io.grpc.ManagedChannel;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.FluxSink;

public class ListServicesHandler implements StreamObserver<ServerReflectionResponse> {
	
	private FluxSink<String> sink;
	private StreamObserver<ServerReflectionRequest> serverStream;
	private ManagedChannel channel;

	
	public ListServicesHandler(FluxSink<String> sink, ManagedChannel channel) {
		this.sink = sink;
		this.channel = channel;
	}

	@Override
	public void onNext(ServerReflectionResponse response) {
		String serviceList = Joiner.on("\n").join(response.getListServicesResponse().getServiceList());
		sink.next(serviceList);
		serverStream.onCompleted();
	}

	@Override
	public void onError(Throwable t) {
		t.printStackTrace();
		System.out.println("### " + sink);
		sink.error(t);
	}

	@Override
	public void onCompleted() {
		System.out.println("completed");
		channel.shutdown();
		sink.complete();
	}

	public void setServerStream(StreamObserver<ServerReflectionRequest> serverStream) {
		this.serverStream = serverStream;
	}


}
