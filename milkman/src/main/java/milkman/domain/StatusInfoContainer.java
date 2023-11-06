package milkman.domain;

import java.util.Map;
import java.util.function.Consumer;
import lombok.Value;
import milkman.domain.ResponseContainer.StyledText;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

public class StatusInfoContainer {

  ReplayProcessor<StatusEntry> emitter = ReplayProcessor.create();

  //for backwards compatibility
  public void complete(Map<String, StyledText> entries) {
    entries.forEach((k,v) -> emitter.onNext(new StatusEntry(k, v)));
    emitter.onComplete();
  }

  //for backwards compatibility
  public Mono<Map<String, StyledText>> toMono() {
    return Flux.from(emitter).collectMap(
        StatusEntry::getKey,
        StatusEntry::getValue
    );
  }

  public void complete() {
    emitter.onComplete();
  }

  public StatusInfoContainer add(String key, String value) {
    add(key, new StyledText(value));
    return this;
  }

  public StatusInfoContainer add(String key, StyledText value) {
    emitter.onNext(new StatusEntry(key, value));
    return this;
  }

  public void subscribe(Consumer<? super StatusEntry> subscriber) {
    emitter.subscribe(subscriber);
  }


  @Value
  public static class StatusEntry {
     String key;
     StyledText value;
  }
}
