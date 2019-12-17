package milkman.ui.plugin.rest.domain;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import milkman.domain.ResponseAspect;
import milkman.ui.plugin.rest.HttpUtil;

@Data
@RequiredArgsConstructor
public class RestResponseHeaderAspect implements ResponseAspect {
	
	private final CompletableFuture<List<HeaderEntry>> entries;
	
	public CompletableFuture<String> contentType() {
		return entries.thenApply(es -> es.stream()
				.filter(e -> e.getName().equalsIgnoreCase("content-type"))
				.map(e -> HttpUtil.extractContentType(e.getValue()))
				.findAny().orElse("text/plain"));
	}
	
	public CompletableFuture<String> get(String headerName) {
		return entries.thenApply(es -> es.stream()
				.filter(e -> e.getName().equalsIgnoreCase(headerName))
				.map(e -> e.getValue())
				.findAny().orElse(""));
	}

	@Override
	public String getName() {
		return "headers";
	}
}
