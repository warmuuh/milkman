
package milkman.plugin.grpc.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A interceptor to handle client header.
 */
@RequiredArgsConstructor
public class HeaderClientInterceptor implements ClientInterceptor {

	private final Map<String, String> requestHeaders;

	@Getter
	private final CompletableFuture<Map<String, String>> responseHeaders = new CompletableFuture<>();
	
	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
			CallOptions callOptions, Channel next) {
		return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				addHeaders(headers, requestHeaders);
				super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
					@Override
					public void onHeaders(Metadata headers) {
						/**
						 * if you don't need receive header from server, you can use
						 * {@link io.grpc.stub.MetadataUtils#attachHeaders} directly to send header
						 */
						readResponseHeaders(headers);
						super.onHeaders(headers);
					}

					protected void readResponseHeaders(Metadata headers) {
						Map<String, String> result = new HashMap<String, String>();
						for (String key : headers.keys()) {
							//we only support ascii headers for now
							String value = headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
							if (value != null) {
								result.put(key, value);
							}
						}
						responseHeaders.complete(result);
					}
				}, headers);
			}

			private void addHeaders(Metadata headers, Map<String, String> requestHeaders) {
				for (Entry<String, String> entry : requestHeaders.entrySet()) {
					Key<String> key = Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER);
					headers.put(key, entry.getValue());
				}
			}
		};
	}
}