package milkman.plugin.grpc.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import me.dinowernli.grpc.polyglot.grpc.DynamicGrpcClient;
import me.dinowernli.grpc.polyglot.grpc.ServerReflectionClient;
import me.dinowernli.grpc.polyglot.io.MessageReader;
import me.dinowernli.grpc.polyglot.protobuf.ProtoMethodName;
import me.dinowernli.grpc.polyglot.protobuf.ServiceResolver;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.plugin.grpc.domain.GrpcPayloadAspect;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.domain.GrpcResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.ui.plugin.Templater;
import reactor.core.publisher.Flux;

public class GrpcRequestProcessor {

	private ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

	@SneakyThrows
	public GrpcResponseContainer executeRequest(GrpcRequestContainer request, Templater templater) {

	    GrpcOperationAspect operationAspect = request.getAspect(GrpcOperationAspect.class).orElseThrow(() -> new IllegalArgumentException("Operation Aspect missing"));
	    GrpcPayloadAspect payloadAspect = request.getAspect(GrpcPayloadAspect.class).orElseThrow(() -> new IllegalArgumentException("Payload Aspect missing"));
	    
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
	   

	    
	    var client = ServerReflectionClient.create(channel);
	    var protoMethod = ProtoMethodName.parseFullGrpcMethodName(operationAspect.getOperation());
	    FileDescriptorSet descriptorSet = client.lookupService(protoMethod.getFullServiceName()).get();
	    var resolver = ServiceResolver.fromFileDescriptorSet(descriptorSet);
	    var methodDefinition = resolver.resolveServiceMethod(protoMethod);
	    
	    TypeRegistry registry = TypeRegistry.newBuilder()
	            .add(resolver.listMessageTypes())
	            .build();
	    
	    MessageReader reader = MessageReader.forStream(IOUtils.toInputStream(payloadAspect.getPayload()), methodDefinition.getInputType(), registry);
	    var requestMessages = reader.read();

	    MessageWriter<DynamicMessage> writer = MessageWriter.create(registry);
	    
	    var dynamicClient  = DynamicGrpcClient.create(methodDefinition, channel);
	    
	    var flux = Flux.<DynamicMessage>create(sink -> {
	    	dynamicClient.call(requestMessages, new StreamObserver<DynamicMessage>() {
				@Override
				public void onNext(DynamicMessage value) {
					sink.next(value);
				}
				
				@Override
				public void onError(Throwable t) {
					t.printStackTrace();
					channel.shutdown();
					sink.error(t);
				}
				
				@Override
				public void onCompleted() {
					channel.shutdown();
					sink.complete();
				}
			}, CallOptions.DEFAULT);
    	});
	    
    	var responsePayloadAspect = new GrpcResponsePayloadAspect(flux.map(writer::convertMessage));
		response.getAspects().add(responsePayloadAspect);
		
		return response;
	}

}
