package milkman.plugin.grpc.processor;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.domain.GrpcResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.ui.plugin.Templater;
import reactor.core.publisher.Flux;

public class GrpcRequestProcessor {

	public GrpcResponseContainer executeRequest(GrpcRequestContainer request, Templater templater) {
		
		String[] split = request.getEndpoint().split(":");
		if (split.length != 2) {
			throw new IllegalArgumentException("Unexpected endpoint format. should be 'host:port'");
		}
		String host = split[0];
		int port = Integer.parseInt(split[1]);
		
		
		var response = new GrpcResponseContainer(request.getEndpoint());
		
		
	    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
	            .usePlaintext()
	            .build();
	   
    	var stub = ServerReflectionGrpc.newStub(channel);
    	var flux = Flux.<String>create(sink -> {
    		var listServicesHandler = new ListServicesHandler(sink, channel);
			StreamObserver<ServerReflectionRequest> serverStream = stub
    					.withDeadlineAfter(10, TimeUnit.SECONDS)
    					.serverReflectionInfo(listServicesHandler);
    		listServicesHandler.setServerStream(serverStream);
    		//send command
			serverStream.onNext(ServerReflectionRequest.newBuilder().setListServices("").build());
    	});
    	
    	var payloadAspect = new GrpcResponsePayloadAspect(flux);
		response.getAspects().add(payloadAspect);
		
		return response;
	}

}
