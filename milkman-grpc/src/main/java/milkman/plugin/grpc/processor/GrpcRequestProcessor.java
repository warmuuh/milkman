package milkman.plugin.grpc.processor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;

import org.apache.commons.io.IOUtils;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import lombok.SneakyThrows;
import me.dinowernli.grpc.polyglot.grpc.DynamicGrpcClient;
import me.dinowernli.grpc.polyglot.protobuf.ProtoMethodName;
import me.dinowernli.grpc.polyglot.protobuf.ProtocInvoker;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.plugin.grpc.domain.GrpcPayloadAspect;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.domain.GrpcResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.plugin.grpc.editor.Subscribers;
import milkman.ui.plugin.Templater;

public class GrpcRequestProcessor extends BaseGrpcProcessor {

	@SneakyThrows
	public GrpcResponseContainer executeRequest(GrpcRequestContainer request, Templater templater) {
	    GrpcOperationAspect operationAspect = request.getAspect(GrpcOperationAspect.class).orElseThrow(() -> new IllegalArgumentException("Operation Aspect missing"));
	    GrpcPayloadAspect payloadAspect = request.getAspect(GrpcPayloadAspect.class).orElseThrow(() -> new IllegalArgumentException("Payload Aspect missing"));
		
		
		var responseStream = makeRequest(request, operationAspect, payloadAspect);
    	
		var responsePayloadAspect = new GrpcResponsePayloadAspect(responseStream);
		var response = new GrpcResponseContainer(request.getEndpoint());
		response.getAspects().add(responsePayloadAspect);
		
		return response;
	}

	protected Publisher<String> makeRequest(GrpcRequestContainer request, GrpcOperationAspect operationAspect,
			GrpcPayloadAspect payloadAspect) throws InterruptedException, ExecutionException {
		ManagedChannel channel = createChannel(request);
	   
	    
	    var protoMethod = ProtoMethodName.parseFullGrpcMethodName(operationAspect.getOperation());
		FileDescriptorSet descriptorSet = operationAspect.isUseReflection() 
						? fetchServiceDescriptionViaReflection(channel, protoMethod) 
						: compileProtoSchema(operationAspect.getProtoSchema(), protoMethod);
						
						
		DynamicMessageDeEncoder deenc = new DynamicMessageDeEncoder(protoMethod, descriptorSet);

	    
		SubmissionPublisher<DynamicMessage> publisher = new SubmissionPublisher<>();
		Publisher<DynamicMessage> buffer = Subscribers.buffer(publisher);
		
		var requestMessages = deenc.deserializeFromJson(payloadAspect.getPayload());
	    var dynamicClient  = DynamicGrpcClient.create(deenc.getMethodDefinition(), channel);
	    dynamicClient.call(requestMessages, new StreamObserverToPublisherBridge<>(publisher, channel), CallOptions.DEFAULT);
    	var responseStream = Subscribers.map(buffer, deenc::serializeToJson);
		return responseStream;
	}

	@SneakyThrows
	private FileDescriptorSet compileProtoSchema(String protoSchema, ProtoMethodName protoMethod) {
		ProtocInvoker protocInvoker = new ProtocInvoker(IOUtils.toInputStream(protoSchema));
		return protocInvoker.invoke();
	}
	
	
	

}
