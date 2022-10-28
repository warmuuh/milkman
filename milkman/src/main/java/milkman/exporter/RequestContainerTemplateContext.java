package milkman.exporter;

import com.samskivert.mustache.DefaultCollector;
import com.samskivert.mustache.Mustache.CustomContext;
import com.samskivert.mustache.Mustache.VariableFetcher;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;

@RequiredArgsConstructor
public class RequestContainerTemplateContext extends DefaultCollector implements CustomContext {

  private final RequestContainer requestContainer;

  @Override
  public Object get(String name) throws Exception {
    VariableFetcher fetcher = createFetcher(requestContainer, name);
    if (fetcher != null) {
      return fetcher.get(requestContainer, name);
    }

    return requestContainer.getAspects().stream()
        .filter(a -> a.getName().equals(name))
        .findAny().orElse(null);
  }
}
