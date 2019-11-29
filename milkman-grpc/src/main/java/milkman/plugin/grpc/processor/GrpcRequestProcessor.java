package milkman.plugin.grpc.processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import lombok.SneakyThrows;
import lombok.Value;
import me.dinowernli.grpc.polyglot.grpc.DynamicGrpcClient;
import me.dinowernli.grpc.polyglot.protobuf.ProtoMethodName;
import me.dinowernli.grpc.polyglot.protobuf.ProtocInvoker;
import milkman.plugin.grpc.domain.GrpcHeaderAspect;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.plugin.grpc.domain.GrpcPayloadAspect;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.domain.GrpcResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponseHeaderAspect;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.plugin.grpc.domain.HeaderEntry;
import milkman.plugin.grpc.editor.Subscribers;
import milkman.ui.plugin.Templater;

public class GrpcRequestProcessor extends BaseGrpcProcessor {

	@SneakyThrows
	public GrpcResponseContainer executeRequest(GrpcRequestContainer request, Templater templater) {
	    GrpcOperationAspect operationAspect = request.getAspect(GrpcOperationAspect.class).orElseThrow(() -> new IllegalArgumentException("Operation Aspect missing"));
	    GrpcPayloadAspect payloadAspect = request.getAspect(GrpcPayloadAspect.class).orElseThrow(() -> new IllegalArgumentException("Payload Aspect missing"));
	    GrpcHeaderAspect headerAspect = request.getAspect(GrpcHeaderAspect.class).orElseThrow(() -> new IllegalArgumentException("Header Aspect missing"));
		
		
		var responseData = makeRequest(request, operationAspect, headerAspect, payloadAspect);
    	
		var response = new GrpcResponseContainer(request.getEndpoint());

		var responsePayloadAspect = new GrpcResponsePayloadAspect(responseData.getBodyStream());
		response.getAspects().add(responsePayloadAspect);
		
		var responseHeaderAspect = new GrpcResponseHeaderAspect(responseData.getHeaderFuture().thenApply(this::convertToEntries));
		response.getAspects().add(responseHeaderAspect);

		
		return response;
	}

	protected ResponseDataHolder makeRequest(GrpcRequestContainer request, 
			GrpcOperationAspect operationAspect,
			GrpcHeaderAspect headerAspect,
			GrpcPayloadAspect payloadAspect) throws InterruptedException, ExecutionException {
	   
		HeaderClientInterceptor clientInterceptor = createHeaderInterceptor(headerAspect);
	    var managedChannel = createChannel(request);
		Channel channel = ClientInterceptors.intercept(managedChannel, clientInterceptor);;
		
		
	    var protoMethod = ProtoMethodName.parseFullGrpcMethodName(operationAspect.getOperation());
		FileDescriptorSet descriptorSet = operationAspect.isUseReflection() 
						? fetchServiceDescriptionViaReflection(channel, protoMethod) 
						: compileProtoSchema(operationAspect.getProtoSchema(), protoMethod);
						
						
		DynamicMessageDeEncoder deenc = new DynamicMessageDeEncoder(protoMethod, descriptorSet);

	    
		SubmissionPublisher<DynamicMessage> publisher = new SubmissionPublisher<>();
		Publisher<DynamicMessage> buffer = Subscribers.buffer(publisher);
		
		var requestMessages = deenc.deserializeFromJson(payloadAspect.getPayload());
	    var dynamicClient  = DynamicGrpcClient.create(deenc.getMethodDefinition(), channel);
	    dynamicClient.call(requestMessages, new StreamObserverToPublisherBridge<>(publisher, () -> managedChannel.shutdown()), CallOptions.DEFAULT);
    	var responseStream = Subscribers.map(buffer, deenc::serializeToJson);
		return new ResponseDataHolder(responseStream, clientInterceptor.getResponseHeaders());
	}
	
	protected List<HeaderEntry> convertToEntries(Map<String, String> headers){
		return headers.entrySet().stream().map(e -> new HeaderEntry("", e.getKey(), e.getValue(), true))
			.collect(Collectors.toList());
	}
	

	protected HeaderClientInterceptor createHeaderInterceptor(GrpcHeaderAspect headerAspect) {
		var requestHeaders = headerAspect.getEntries().stream()
				.filter(e -> e.isEnabled())
				.collect(Collectors.toMap(e -> e.getName(), e -> e.getValue()));
		return new HeaderClientInterceptor(requestHeaders);
	}

	@SneakyThrows
	private FileDescriptorSet compileProtoSchema(String protoSchema, ProtoMethodName protoMethod) {
		ProtocInvoker protocInvoker = new ProtocInvoker(IOUtils.toInputStream(protoSchema));
		return protocInvoker.invoke();
	}
	
	@Value
	static class ResponseDataHolder{
		Publisher<String> bodyStream;
		CompletableFuture<Map<String, String>> headerFuture;
	}
	
	
	

}
