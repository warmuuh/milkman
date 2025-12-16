package milkman.plugin.mcp.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Data
public class McpStructuredOutputAspect implements ResponseAspect {

	Sinks.Many<String> structuredOutput = Sinks.many().replay().latest();

	public void setStructuredOutput(String latestResponse) {
		structuredOutput.tryEmitNext(latestResponse);
	}

	public Flux<String> getStructuredOutput() {
		return structuredOutput.asFlux();
	}
	
	@Override
	public String getName() {
		return "structuredOutput";
	}
}
