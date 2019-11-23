package milkman.plugin.grpc.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.SneakyThrows;
import me.dinowernli.grpc.polyglot.grpc.ServerReflectionClient;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.plugin.grpc.domain.GrpcResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponsePayloadAspect;
import milkman.ui.plugin.Templater;
import reactor.core.publisher.Flux;

public class GrpcMetaProcessor {

	private ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

	@SneakyThrows
	public GrpcResponseContainer listServices(GrpcRequestContainer request, Templater templater) {

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
	    

	    var flux = Flux.<String>create(sink -> {
			Futures.addCallback(client.listServices(), new FutureCallback<ImmutableList<String>>() {

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
    	});
    	
    	var responsePayloadAspect = new GrpcResponsePayloadAspect(flux);
		response.getAspects().add(responsePayloadAspect);
		
		return response;
	}

}
