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
	
	private final List<HeaderEntry> entries;
	
	public String contentType() {
		return entries.stream()
				.filter(e -> e.getName().equalsIgnoreCase("content-type"))
				.map(e -> HttpUtil.extractContentType(e.getValue()))
				.findAny().orElse("text/plain");
	}
	
	public String get(String headerName) {
		return entries.stream()
				.filter(e -> e.getName().equalsIgnoreCase(headerName))
				.map(e -> e.getValue())
				.findAny().orElse("");
	}

	@Override
	public String getName() {
		return "headers";
	}
}
