package milkman.plugin.mcp.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Data
public class McpInstructionsAspect implements ResponseAspect {

	Sinks.Many<String> instructions = Sinks.many().replay().latest();

	public void setInstructions(String latestResponse) {
		instructions.tryEmitNext(latestResponse);
	}

	public Flux<String> getInstructions() {
		return instructions.asFlux();
	}
	
	@Override
	public String getName() {
		return "instructions";
	}
}
