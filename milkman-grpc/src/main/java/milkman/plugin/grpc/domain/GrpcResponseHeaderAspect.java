package milkman.plugin.grpc.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.AllArgsConstructor;
import lombok.Data;
import milkman.domain.ResponseAspect;

@Data
@AllArgsConstructor
public class GrpcResponseHeaderAspect implements ResponseAspect {
	
	private CompletableFuture<List<HeaderEntry>> entries;
	
//	public String contentType() {
//		return entries.stream()
//				.filter(e -> e.getName().equalsIgnoreCase("content-type"))
//				.map(e -> HttpUtil.extractContentType(e.getValue()))
//				.findAny().orElse("text/plain");
//	}

//	public String get(String headerName) {
//		return entries.stream()
//				.filter(e -> e.getName().equalsIgnoreCase(headerName))
//				.map(e -> e.getValue())
//				.findAny().orElse("");
//	}

	@Override
	public String getName() {
		return "headers";
	}
}
