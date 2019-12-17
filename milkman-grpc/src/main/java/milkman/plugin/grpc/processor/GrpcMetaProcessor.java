package milkman.plugin.grpc.processor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

import io.grpc.ManagedChannel;
import lombok.SneakyThrows;
import me.dinowernli.grpc.polyglot.grpc.ServerReflectionClient;
import me.dinowernli.grpc.polyglot.protobuf.ProtoMethodName;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.domain.GrpcResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.plugin.grpc.processor.ProtoDescriptorSerializer.FileContent;
import milkman.ui.plugin.Templater;
import milkman.utils.reactive.Subscribers;

public class GrpcMetaProcessor extends BaseGrpcProcessor {

	private ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

	@SneakyThrows
	public GrpcResponseContainer listServices(GrpcRequestContainer request, Templater templater) {
		var response = new GrpcResponseContainer(request.getEndpoint());
		
	    SubmissionPublisher<String> publisher = fetchServiceList(request);
	  
    	var responsePayloadAspect = new GrpcResponsePayloadAspect(publisher);
		response.getAspects().add(responsePayloadAspect);
		
		return response;
	}

	@SneakyThrows
	public GrpcResponseContainer showServiceDefinition(GrpcRequestContainer request, Templater templater) {
		GrpcOperationAspect operationAspect = request.getAspect(GrpcOperationAspect.class).orElseThrow(() -> new IllegalArgumentException("Operation Aspect missing"));
	    var protoMethod = ProtoMethodName.parseFullGrpcMethodName(operationAspect.getOperation());

	    
		
	    Publisher<String> publisher = fetchServiceDefinition(request, protoMethod);

	    
	    var response = new GrpcResponseContainer(request.getEndpoint());
    	var responsePayloadAspect = new GrpcResponsePayloadAspect(publisher);
		response.getAspects().add(responsePayloadAspect);
		
		return response;
	}

	
	
	@SneakyThrows
	private Publisher<String> fetchServiceDefinition(GrpcRequestContainer request, ProtoMethodName protoMethod) {
		ManagedChannel channel = createChannel(request);
		FileDescriptorSet descriptorSet = fetchServiceDescriptionViaReflection(channel, protoMethod);
		
		String protoContent = toProto(descriptorSet);
		
		SubmissionPublisher<String> publisher = new SubmissionPublisher<String>();
		var buffer = Subscribers.buffer(publisher);
		publisher.submit(protoContent);
		
		return buffer;
	}

	private String toProto(FileDescriptorSet descriptorSet) {
		ProtoDescriptorSerializer serializer = new ProtoDescriptorSerializer();
		List<FileContent> files = serializer.descriptorToString(descriptorSet);
		return files.stream()
			.map((FileContent f) ->  "/* Filename: " + f.getFileName() + " */\n" +f.getContents())
			.collect(Collectors.joining("\n---\n"));
	}

	protected SubmissionPublisher<String> fetchServiceList(GrpcRequestContainer request) {
		ManagedChannel channel = createChannel(request);
	    var client = ServerReflectionClient.create(channel);

	    
	    SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
		
 
	    Futures.addCallback(client.listServices(), new FutureCallback<ImmutableList<String>>() {

			@Override
			public void onSuccess(ImmutableList<String> result) {
				result.forEach(publisher::submit);
				channel.shutdown();
				publisher.close();
			}

			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
				channel.shutdown();
				publisher.closeExceptionally(t);
			}
		}, executor);
		return publisher;
	}

}
