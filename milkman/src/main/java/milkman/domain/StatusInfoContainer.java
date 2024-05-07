package milkman.domain;

import java.util.Comparator;
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

  public void complete() {
    emitter.onComplete();
  }

  public StatusInfoContainer add(String key, String value) {
    add(key, new StyledText(value));
    return this;
  }
  public StatusInfoContainer add(String key, Map<String, String> values) {
    emitter.onNext(new StatusEntry(key, values));
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
     Map<String, String> valueMap;

    public StatusEntry(String key, StyledText value) {
      this.key = key;
      this.value = value;
      valueMap = null;
    }

    public StatusEntry(String key, Map<String, String> valueMap) {
      this.key = key;
      this.valueMap = valueMap;
      value = null;
    }

    public boolean isGroup() {
       return valueMap != null;
     }
  }
}
