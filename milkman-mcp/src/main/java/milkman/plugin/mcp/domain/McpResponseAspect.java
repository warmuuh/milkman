package milkman.plugin.mcp.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Data
public class McpResponseAspect implements ResponseAspect {

	Sinks.Many<String> response = Sinks.many().replay().latest();

	public void setResponse(String latestResponse) {
		response.tryEmitNext(latestResponse);
	}

	public Flux<String> getResponse() {
		return response.asFlux();
	}
	
	@Override
	public String getName() {
		return "result";
	}
}
