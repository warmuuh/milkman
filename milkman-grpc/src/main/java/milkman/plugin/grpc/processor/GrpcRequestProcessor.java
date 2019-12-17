package milkman.plugin.grpc.processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.StatusRuntimeException;
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
import milkman.utils.AsyncResponseControl.AsyncControl;

public class GrpcRequestProcessor extends BaseGrpcProcessor {

	@SneakyThrows
	public GrpcResponseContainer executeRequest(GrpcRequestContainer request, Templater templater, AsyncControl asyncControl) {
	    GrpcOperationAspect operationAspect = request.getAspect(GrpcOperationAspect.class).orElseThrow(() -> new IllegalArgumentException("Operation Aspect missing"));
	    GrpcPayloadAspect payloadAspect = request.getAspect(GrpcPayloadAspect.class).orElseThrow(() -> new IllegalArgumentException("Payload Aspect missing"));
	    GrpcHeaderAspect headerAspect = request.getAspect(GrpcHeaderAspect.class).orElseThrow(() -> new IllegalArgumentException("Header Aspect missing"));
		
		
		var responseData = makeRequest(request, operationAspect, headerAspect, payloadAspect, asyncControl);
    	
		var response = new GrpcResponseContainer(request.getEndpoint());

		var responsePayloadAspect = new GrpcResponsePayloadAspect(responseData.getBodyStream());
		response.getAspects().add(responsePayloadAspect);
		
		var responseHeaderAspect = new GrpcResponseHeaderAspect(responseData.getHeaderFuture().thenApply(this::convertToEntries));
		response.getAspects().add(responseHeaderAspect);

		responseData.getRequestTime().thenAccept(t -> response.getStatusInformations().complete(Map.of("Time", t + "ms")));
		
		
		
		return response;
	}

	protected ResponseDataHolder makeRequest(GrpcRequestContainer request, 
			GrpcOperationAspect operationAspect,
			GrpcHeaderAspect headerAspect,
			GrpcPayloadAspect payloadAspect, 
			AsyncControl asyncControl) throws InterruptedException, ExecutionException {
	   
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
	    long startTime = System.currentTimeMillis();
	    CompletableFuture<Long> requestTime = new CompletableFuture<Long>();
	    asyncControl.triggerReqeuestStarted();
	    var streamObserver = new StreamObserverToPublisherBridge<>(publisher, () -> managedChannel.shutdown());
		var callFuture = dynamicClient.call(requestMessages, streamObserver, CallOptions.DEFAULT);
	    
	    asyncControl.onCancellationRequested.add(streamObserver::cancel);
	    
	    Futures.addCallback(callFuture, new FutureCallback<Void>() {
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
	    
	    
    	var responseStream = Subscribers.map(buffer, deenc::serializeToJson);
		return new ResponseDataHolder(responseStream, clientInterceptor.getResponseHeaders(), requestTime);
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
		CompletableFuture<Long> requestTime;
	}
	
	
	

}
