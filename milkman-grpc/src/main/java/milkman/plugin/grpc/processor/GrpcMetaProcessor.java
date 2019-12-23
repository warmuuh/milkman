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
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;

public class GrpcMetaProcessor extends BaseGrpcProcessor {

	private ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

	@SneakyThrows
	public GrpcResponseContainer listServices(GrpcRequestContainer request, Templater templater) {
		var response = new GrpcResponseContainer(request.getEndpoint());

		ReplayProcessor<String> processor = ReplayProcessor.create();
		fetchServiceList(processor.sink(), request);
	  
    	var responsePayloadAspect = new GrpcResponsePayloadAspect(processor);
		response.getAspects().add(responsePayloadAspect);
		
		return response;
	}

	@SneakyThrows
	public GrpcResponseContainer showServiceDefinition(GrpcRequestContainer request, Templater templater) {
		GrpcOperationAspect operationAspect = request.getAspect(GrpcOperationAspect.class).orElseThrow(() -> new IllegalArgumentException("Operation Aspect missing"));
	    var protoMethod = ProtoMethodName.parseFullGrpcMethodName(operationAspect.getOperation());

		ReplayProcessor<String> processor = ReplayProcessor.create();
	    fetchServiceDefinition(processor.sink(), request, protoMethod);

	    var response = new GrpcResponseContainer(request.getEndpoint());
    	var responsePayloadAspect = new GrpcResponsePayloadAspect(processor);
		response.getAspects().add(responsePayloadAspect);
		
		return response;
	}

	
	
	@SneakyThrows
	private void fetchServiceDefinition(FluxSink<String> sink, GrpcRequestContainer request, ProtoMethodName protoMethod) {
		ManagedChannel channel = createChannel(request);
		FileDescriptorSet descriptorSet = fetchServiceDescriptionViaReflection(channel, protoMethod);
		String protoContent = toProto(descriptorSet);
		sink.next(protoContent);
		sink.complete();
	}

	private String toProto(FileDescriptorSet descriptorSet) {
		ProtoDescriptorSerializer serializer = new ProtoDescriptorSerializer();
		List<FileContent> files = serializer.descriptorToString(descriptorSet);
		return files.stream()
			.map((FileContent f) ->  "/* Filename: " + f.getFileName() + " */\n" +f.getContents())
			.collect(Collectors.joining("\n---\n"));
	}

	protected void fetchServiceList(FluxSink<String> sink, GrpcRequestContainer request) {
		ManagedChannel channel = createChannel(request);
	    var client = ServerReflectionClient.create(channel);

	    
	    Futures.addCallback(client.listServices(), new FutureCallback<>() {

			@Override
			public void onSuccess(ImmutableList<String> result) {
				result.forEach(sink::next);
				channel.shutdown();
				sink.complete();
			}

			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
				channel.shutdown();
				sink.error(t);
			}
		}, executor);
	}

}
