package milkman.plugin.grpc.processor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
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
import milkman.domain.ResponseContainer.StyledText;
import milkman.plugin.grpc.domain.*;
import milkman.ui.plugin.Templater;
import milkman.utils.AsyncResponseControl.AsyncControl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GrpcRequestProcessor extends BaseGrpcProcessor {

	@SneakyThrows
	public GrpcResponseContainer executeRequest(GrpcRequestContainer request, Templater templater, AsyncControl asyncControl) {
	    GrpcOperationAspect operationAspect = request.getAspect(GrpcOperationAspect.class).orElseThrow(() -> new IllegalArgumentException("Operation Aspect missing"));
	    validate(operationAspect);

	    GrpcPayloadAspect payloadAspect = request.getAspect(GrpcPayloadAspect.class).orElseThrow(() -> new IllegalArgumentException("Payload Aspect missing"));
	    GrpcHeaderAspect headerAspect = request.getAspect(GrpcHeaderAspect.class).orElseThrow(() -> new IllegalArgumentException("Header Aspect missing"));
		
		
		var responseData = makeRequest(request, templater, operationAspect, headerAspect, payloadAspect, asyncControl);
    	
		var response = new GrpcResponseContainer(request.getEndpoint());

		var responsePayloadAspect = new GrpcResponsePayloadAspect(responseData.getBodyStream());
		response.getAspects().add(responsePayloadAspect);
		
		var responseHeaderAspect = new GrpcResponseHeaderAspect(responseData.getHeaderFuture().thenApply(this::convertToEntries));
		response.getAspects().add(responseHeaderAspect);

		responseData.getRequestTime().thenAccept(t -> response.getStatusInformations().complete(Map.of("Time", new StyledText(t + "ms"))));
		return response;
	}

	private void validate(GrpcOperationAspect operationAspect) {
		if (StringUtils.isBlank(operationAspect.getOperation())){
			throw new IllegalArgumentException("No Grpc Operation to call provided");
		}

		if (!operationAspect.isUseReflection() && StringUtils.isBlank(operationAspect.getProtoSchema())) {
			throw new IllegalArgumentException("No Protobuf schema provided and grpc-reflection not enabled");
		}
	}

	protected ResponseDataHolder makeRequest(GrpcRequestContainer request,
											 Templater templater,
											 GrpcOperationAspect operationAspect,
											 GrpcHeaderAspect headerAspect,
											 GrpcPayloadAspect payloadAspect,
											 AsyncControl asyncControl) throws InterruptedException, ExecutionException {
	   
		HeaderClientInterceptor clientInterceptor = createHeaderInterceptor(headerAspect, templater);
	    var managedChannel = createChannel(request, templater);
		Channel channel = ClientInterceptors.intercept(managedChannel, clientInterceptor);
		
		
	    var protoMethod = ProtoMethodName.parseFullGrpcMethodName(operationAspect.getOperation());
		FileDescriptorSet descriptorSet = operationAspect.isUseReflection() 
						? fetchServiceDescriptionViaReflection(channel, protoMethod.getFullServiceName())
						: compileProtoSchema(operationAspect.getProtoSchema(), protoMethod);
						
						
		DynamicMessageDeEncoder deenc = new DynamicMessageDeEncoder(protoMethod, descriptorSet);


		ReplayProcessor<DynamicMessage> publisher = ReplayProcessor.create();

		var requestMessages = deenc.deserializeFromJson(templater.replaceTags(payloadAspect.getPayload()));
	    var dynamicClient  = DynamicGrpcClient.create(deenc.getMethodDefinition(), channel);
	    long startTime = System.currentTimeMillis();
	    CompletableFuture<Long> requestTime = new CompletableFuture<>();
	    asyncControl.triggerReqeuestStarted();
	    var streamObserver = new StreamObserverToPublisherBridge<>(publisher.sink(), () -> managedChannel.shutdown());
		var callFuture = dynamicClient.call(requestMessages, streamObserver, CallOptions.DEFAULT);
	    
	    asyncControl.onCancellationRequested.add(streamObserver::cancel);
	    
	    Futures.addCallback(callFuture, new FutureCallback<>() {
			@Override
			public void onSuccess(Void result) {
				requestTime.complete(System.currentTimeMillis() - startTime);
				asyncControl.triggerRequestSucceeded();
			}

			@Override
			public void onFailure(Throwable t) {
				requestTime.complete(System.currentTimeMillis() - startTime);
				asyncControl.triggerRequestFailed(t);
			}
		}, MoreExecutors.directExecutor());
	    
	    
    	var responseStream = publisher.map(deenc::serializeToJson).map(String::getBytes);
		return new ResponseDataHolder(responseStream, clientInterceptor.getResponseHeaders(), requestTime);
	}
	
	protected List<HeaderEntry> convertToEntries(Map<String, String> headers){
		return headers.entrySet().stream().map(e -> new HeaderEntry("", e.getKey(), e.getValue(), true))
			.collect(Collectors.toList());
	}
	

	protected HeaderClientInterceptor createHeaderInterceptor(GrpcHeaderAspect headerAspect, Templater templater) {
		var requestHeaders = headerAspect.getEntries().stream()
				.filter(e -> e.isEnabled())
				.collect(Collectors.toMap(e -> templater.replaceTags(e.getName()), e -> templater.replaceTags(e.getValue())));
		return new HeaderClientInterceptor(requestHeaders);
	}

	@SneakyThrows
	private FileDescriptorSet compileProtoSchema(String protoSchema, ProtoMethodName protoMethod) {
		ProtocInvoker protocInvoker = new ProtocInvoker(IOUtils.toInputStream(protoSchema));
		return protocInvoker.invoke();
	}
	
	@Value
	static class ResponseDataHolder{
		Flux<byte[]> bodyStream;
		CompletableFuture<Map<String, String>> headerFuture;
		CompletableFuture<Long> requestTime;
	}
	
	
	

}
