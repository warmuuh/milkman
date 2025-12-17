package milkman.plugin.mcp.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Data
public class McpEventsAspect implements ResponseAspect {

	Sinks.Many<String> events = Sinks.many().replay().all();

	public void addEvent(String event) {
		events.tryEmitNext(event);
	}

	public Flux<String> getEvents() {
		return events.asFlux();
	}
	
	@Override
	public String getName() {
		return "events";
	}
}
